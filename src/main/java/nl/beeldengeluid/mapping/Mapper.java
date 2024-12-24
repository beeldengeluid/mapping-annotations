/*
 * Copyright (C) 2024 Licensed under the Apache License, Version 2.0
 */
package nl.beeldengeluid.mapping;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import nl.beeldengeluid.mapping.annotations.Source;
import nl.beeldengeluid.mapping.impl.*;

import static nl.beeldengeluid.mapping.annotations.Source.UNSET;
import static nl.beeldengeluid.mapping.impl.Util.*;

/**
 * Utilities to do the actual mapping using {@link Source}
 * A {@code Mappper} is thread safe. It only contains (unmodifiable) configuration.
 * <p>
 * New mappers (with different configuration) can be created using {@link #builder()} or using 'withers' ({@link #withClearsJsonCacheEveryTime(boolean)} (boolean)}) from an existing one.
 *
 * @author Michiel Meeuwissen
 * @since 0.1
 */
@Slf4j
@AllArgsConstructor
@lombok.Builder(buildMethodName = "build")
public class Mapper {

    /**
     * The default {@link Mapper} instance. Mappers are stateless, but can contain some configuration.
     */
    public static final Mapper MAPPER = Mapper.builder().build();

    /**
     * The Mapper currently effective. A {@link ThreadLocal}. Defaults to {@link #MAPPER}
     */
    private static final ThreadLocal<Mapper> CURRENT = ThreadLocal.withInitial(() -> MAPPER);

    /**
     * @return  The currently in use (thread local) {@link Mapper} instance
     */
    public static Mapper current() {
        return CURRENT.get();
    }

    @With
    @Getter
    private final boolean clearsJsonCacheEveryTime;


    @With(AccessLevel.PACKAGE)
    @lombok.Builder.Default
    @Getter
    private final List<LeafMapper> leafMappers = Stream.of(
        UnwrapCollectionsMapper.INSTANCE,
        JaxbMapper.INSTANCE,
        new EnumMapper(true),
        RecursiveMapper.INSTANCE
        ).sorted().toList();


    /**
     * Creates a new instance (using the no-args constructor) and copies all {@link Source} annotated fields (that match) from source to it.
     * @param source The source object copy data from
     * @param destinationClass The class to create a destination object for
     * @param groups If not empty, only mapping is done if one (or more) of the given groups matches one of the groups of the source annotations.
     * @param <T> Type of the destination object
     * @see #map(Object, Object, Class...)
     * @return a new object of class {@code destinationClass} which all fields filled that are found in {@code source}
     */
    public <T> T map(Object source, Class<T> destinationClass, Class<?>... groups)  {
        T destination = newInstance(destinationClass);
        map(source, destination, groups);
        return destination;
    }



    public <T> T newInstance(Class<T> destinationClass)  {
        try {
            return destinationClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new MapException(e);
        }
    }



    /**
     * Maps all fields in {@code destination} that are annotated with a {@link Source} that matched a field in {@code source}
     * @param source The source object
     * @param destination The destination object
     * @param groups If not empty, only mapping is done if one (or more) of the given groups matches one of the groups of the source annotations.
     */
    public void map(Object source, Object destination, Class<?>... groups) {
        try {
            CURRENT.set(this);
            privateMap(source, destination, destination.getClass(), groups);
        } finally {
            CURRENT.remove();
            if (clearsJsonCacheEveryTime) {
                JsonUtil.clearCache();
            }
        }
    }

    public boolean canMap(Object source, Class<?> destinationClass, Class<?>... groups) {
        try {
            if (!destinationClass.isInstance(source)) {
                Constructor<?> declaredConstructor = destinationClass.getDeclaredConstructor();
                return true;
            }
        } catch (ReflectiveOperationException e) {
        }
        return false;
    }

    /**
     * Just like {@link #map(Object, Object, Class[])}, but the json cache will not be deleted, and {@link #CURRENT} will not be
     * set nor removed. This is basically meant to be called by sub mappings.
     * @param source The source object
     * @param destination The destination object
     * @param groups If not empty, only mapping is done if one (or more) of the given groups matches one of the groups of the source annotations.
     */
     public void subMap(Object source, Object destination, Class<?> destinationClass, Class<?>... groups) {
         privateMap(source, destination, destinationClass, groups);
     }

    /**
     * Given a {@code sourceClass} and a {@code destinationClass} will indicate which fields  (in the destination) will be mapped.
     * @param sourceClass Class of a source object
     * @param destinationClass Class of a destination object
     * @param groups If not empty, only mapping is done if one (or more) of the given groups matches one of the groups of the source annotations.
     */
    public Map<String, Field> getMappedDestinationProperties(Class<?> sourceClass, Class<?> destinationClass, Class<?>... groups) {
        Map<String, Field> result = new HashMap<>();
        Class<?> superClass = sourceClass.getSuperclass();
        if (superClass != null) {
            result.putAll(getMappedDestinationProperties(superClass, destinationClass));
        }
        for (Field field : destinationClass.getDeclaredFields()) {
            getAnnotation(sourceClass, destinationClass, field, groups)
                .ifPresent(a -> result.put(field.getName(), field));
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Returns a function that will use reflection get the value from a source object that maps to the destination field.
     *
     * @param sourceClass      Class of a source object
     * @param destinationField Field of the destination
     * @param destinationClass Field of the destination
     * @param groups           If not empty, only mapping is done if one (or more) of the given groups matches one of the groups of the source annotations.
     */
    public Optional<Function<Object, Optional<Object>>> sourceGetter(Class<?> sourceClass, Field destinationField, Class<?> destinationClass, Class<?>... groups) {
        Map<Class<?>, Optional<Function<Object, Optional<Object>>>> c = GETTER_CACHE.computeIfAbsent(destinationField, (fi) -> new ConcurrentHashMap<>());
        return c.computeIfAbsent(sourceClass, cl -> _sourceGetter(destinationClass, destinationField, sourceClass));

    }



    public Mapper withLeafMapper(LeafMapper instance) {
        List<LeafMapper> list = new ArrayList<>(leafMappers);
        if (!list.contains(instance)) {
            list.add(instance);
            list.sort(Comparator.naturalOrder());
            return withLeafMappers(list);
        }
        return this;
    }

    public <S, D> Mapper withLeafMapper(Class<S> source, Class<D> destination, Function<S, D> function) {
        return withLeafMapper(new LeafMapper() {
            @Override
            public Leaf map(Mapper mapper, MappedField destinationField, Object o) {
                if (destination.isAssignableFrom(destinationField.type()) && source.isInstance(o)) {
                    return LeafMapper.mapped(function.apply((S) o));
                }
                return LeafMapper.NOT_MAPPED;
            }
        });
    }

    public Mapper withoutLeafMapper(LeafMapper instance) {
        List<LeafMapper> list = new ArrayList<>(leafMappers);
        if (list.removeIf(v -> v.equals(instance))) {
            return withLeafMappers(list);
        }
        return this;
    }

    public Mapper withSupportsJaxbAnnotations(Boolean supportsJaxbAnnotations) {
        if (supportsJaxbAnnotations) {
            return withLeafMapper(new EnumMapper(true))
                .withoutLeafMapper(new EnumMapper(false))
                .withLeafMapper(JaxbMapper.INSTANCE);
        } else {
            return withLeafMapper(new EnumMapper(false))
                .withoutLeafMapper(new EnumMapper(true))
                .withoutLeafMapper(JaxbMapper.INSTANCE);
        }
    }

    ///  PRIVATE METHODS

    /**
     * Helper method for {@link #map(Object, Object, Class...)}, recursively called for the class and superclass of the destination
     * object.
     */
    private void privateMap(Object source, Object destination, Class<?> forClass, Class<?>... groups) {
        Class<?> sourceClass = source.getClass();
        Class<?> superClass = forClass.getSuperclass();
        if (superClass != null) {
            privateMap(source, destination, superClass, groups);
        }
        for (Field f: forClass.getDeclaredFields()) {
            getAndSet(f, sourceClass, source, destination, groups);
        }
    }


    /**
     * For a field in the destination object, try to get value from the source, and set
     * this value in destination. Or do nohting if there is no match found
     */
    private void getAndSet(
        Field destinationField,
        Class<?> sourceClass,
        Object source,
        Object destination,
        Class<?>... groups) {
        Optional<Function<Object, Optional<Object>>> getter = sourceGetter(sourceClass, destinationField, destination.getClass(), groups);

        if (getter.isPresent()) {
            Optional<Object> value = getter.get().apply(source);
            value.ifPresentOrElse(v ->
                    destinationSetter(destination.getClass(), destinationField, sourceClass).accept(destination, v),
                () -> log.debug("No field found for {} ({}) {}", destinationField.getName(), getAllSourceAnnotations(destinationField), sourceClass));
        } else {
            log.debug("Ignored destination field {} (No (matching) @Source annotation for {})", destinationField, sourceClass);
        }
    }


    private final Map<Field, Map<Class<?>, Optional<Function<Object, Optional<Object>>>>> GETTER_CACHE = new ConcurrentHashMap<>();



    /**
     * Uncached version of {@link #sourceGetter(Class, Field, Class, Class[])}
     */
    private Optional<Function<Object, Optional<Object>>> _sourceGetter(Class<?> destinationClass, Field destinationField, Class<?> sourceClass, Class<?>... groups) {
        Optional<EffectiveSource> annotation = getAnnotation(sourceClass, destinationClass, destinationField, groups);
        if (annotation.isPresent()) {
            final EffectiveSource s = annotation.get();
            String sourceFieldName = s.field();

            // TODO, may be we can just consider also 'json' values as 'leaf' mappers,
            // and all this stuff may get cleaner.

            if (isJsonField(sourceClass)) {
              return Optional.of(JsonUtil.valueFromJsonGetter(s));
            }
            if (UNSET.equals(sourceFieldName)) {
                sourceFieldName = destinationField.getName();
            }
            Optional<Field> sourceField = getSourceField(sourceClass, sourceFieldName);
            if (sourceField.isPresent()) {
                final Field sf = sourceField.get();

                if (UNSET.equals(s.jsonPointer()) && UNSET.equals(s.jsonPath())) {
                    return Optional.of(source -> getSourceValue(source, sf, s.path()));
                } else {
                    return Optional.of(source -> JsonUtil.getSourceJsonValue(s, source, sf, destinationField));
                }
            }
        }
        return Optional.empty();
    }

    private final Map<Class<?>, Map<Field, Map<Class<?>, BiConsumer<Object, Object>>>> SETTER_CACHE = new ConcurrentHashMap<>();

    /**
     * Returns a BiConsumer, that for a certain {@code destinationField} consumes a destination object, and sets a value
     * for the given field.
     * @param destinationField The field to set
     * @param sourceClass The currently matched class of the source object
     */
    private  BiConsumer<Object, Object> destinationSetter(Class<?> destinationClass, Field destinationField, Class<?> sourceClass) {
        Map<Field, Map<Class<?>, BiConsumer<Object, Object>>> classCache = SETTER_CACHE.computeIfAbsent(destinationClass, fi -> new ConcurrentHashMap<>());
        Map<Class<?>, BiConsumer<Object, Object>> cache = classCache.computeIfAbsent(destinationField, fi -> new ConcurrentHashMap<>());
        return cache.computeIfAbsent(sourceClass, c -> _destinationSetter(destinationClass, destinationField, c));
    }

    /**
     * Uncached version of {@link #destinationSetter(Class, Field, Class)}
     */
    private  BiConsumer<Object, Object> _destinationSetter(Class<?> destinationClass, Field destinationField, Class<?> sourceClass) {
        Optional<EffectiveSource> annotation = getAnnotation(sourceClass, destinationClass, destinationField);
        if (annotation.isPresent()) {
            EffectiveSource effectiveSource = annotation.get();
            String sourceFieldName = effectiveSource.field();
            if (isJsonField(sourceClass)) {
                destinationField.setAccessible(true);
                return (destination, o) -> {
                    try {
                        MappedField f = MappedField.of(destinationField, effectiveSource);
                        destinationField.set(destination, mapLeaf(f, o));
                    } catch (Exception e) {
                        log.warn("When setting {} in {}: {}", o, destinationField, e.getMessage());
                    }
                };
            }
            if (UNSET.equals(sourceFieldName)) {
                sourceFieldName = destinationField.getName();
            }
            Optional<Field> sourceField = getSourceField(sourceClass, sourceFieldName);
            if (sourceField.isPresent()) {
                destinationField.setAccessible(true);
                return (destination, o) -> {
                    try {
                        MappedField f = MappedField.of(destinationField, effectiveSource);
                        Object convertedValue = mapLeaf(f, o);
                        destinationField.set(destination, convertedValue);
                    } catch (Exception e) {
                        log.warn("When setting '{}' in {}: {}", o, destinationField, e.getMessage());
                    }
                };
            }
        }
        return (d, v) -> {};
    }


    public Object mapLeaf(MappedField destinationField, Object o) {
        for (LeafMapper valueMapper : leafMappers) {
            LeafMapper.Leaf result = valueMapper.map(this, destinationField, o);
            if (result.success()) {
                o = result.result();
                if (result.terminate()) {
                    break;
                }
            }
        }
        return o;
    }



}
