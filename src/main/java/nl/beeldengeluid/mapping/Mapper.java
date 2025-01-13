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
import java.util.function.Function;
import java.util.stream.Stream;

import nl.beeldengeluid.mapping.annotations.Source;
import nl.beeldengeluid.mapping.impl.*;

import org.meeuw.functional.Consumers;
import org.meeuw.functional.TriConsumer;

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
     * A new mapper can be constructed using {@link #builder()} or by using {@code with-} methods on an existing one.
     */
    public static final Mapper MAPPER = Mapper.builder().build();

    @With
    @Getter
    private final boolean clearsJsonCacheEveryTime;


    @With(AccessLevel.PACKAGE)
    @lombok.Builder.Default
    @Getter
    private final List<LeafMapper> leafMappers = Stream.of(
        UnwrapCollectionsMapper.INSTANCE,
        JaxbMapper.INSTANCE,
        ScalarMapper.INSTANCE,
        new EnumMapper(true, false),
        RecursiveMapper.INSTANCE
        ).sorted().toList();


    /**
     * Creates a new instance (using the no-args constructor) and copies all {@link Source} annotated fields (that match) from source to it.
     * @param source The source object copy data from
     * @param destinationClass The class to create a destination object for

     * @param <T> Type of the destination object
     * @see #map(Object, Object)
     * @return a new object of class {@code destinationClass} which all fields filled that are found in {@code source}
     */
    public <T> T map(Object source, Class<T> destinationClass)  {
        T destination = newInstance(destinationClass);
        map(source, destination);
        return destination;
    }


    /**
     * Support for instantiating new objects.
     * Currently just calls the default no-args constructor.
     * @param destinationClass
     * @return A new object
     * @param <T>
     */

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
     */
    public void map(Object source, Object destination) {
        try {
            privateMap(source, destination, destination.getClass());
        } finally {
            if (clearsJsonCacheEveryTime) {
                JsonUtil.clearCache();
            }
        }
    }


    /**
     * For now just checks whether there is a no args accessible constructor in the destination clas..
     * If so, that suffices to conclude that this mapper can map to it.
     * <p>
     * TODO: This seems too simple? Should the leaf mappers have a say in this or so?

     *
     * @param source
     * @param destinationClass
     * @param groups
     * @return
     */
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
     * Just like {@link #map(Object, Object)}, but for example the json cache will not be cleared (if {@link #clearsJsonCacheEveryTime}). This is basically meant to be called by sub mappings.
     * @param source The source object
     * @param destination The destination object
     */
     public void subMap(Object source, Object destination, Class<?> destinationClass) {
         privateMap(source, destination, destinationClass);
     }

    /**
     * Given a {@code sourceClass} and a {@code destinationClass} will indicate which fields  (in the destination) will be mapped.
     * @param sourceClass Class of a source object
     * @param destinationClass Class of a destination object
     */
    public Map<String, Field> getMappedDestinationProperties(Class<?> sourceClass, Class<?> destinationClass) {
        Map<String, Field> result = new HashMap<>();
        Class<?> superClass = sourceClass.getSuperclass();
        if (superClass != null) {
            result.putAll(getMappedDestinationProperties(superClass, destinationClass));
        }
        for (Field field : destinationClass.getDeclaredFields()) {
            getAnnotation(sourceClass, destinationClass, field).forEach(a -> result.put(field.getName(), field));
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Returns a function that will use reflection get the value from a source object that maps to the destination field.
     *
     * @param sourceClass      Class of a source object
     * @param destinationField Field of the destination
     * @param destinationClass Field of the destination
     */
    public Optional<Function<Object, Optional<ValueAndEffectiveSource>>> sourceGetter(Class<?> sourceClass, Field destinationField, Class<?> destinationClass) {
        Map<Class<?>, Optional<Function<Object, Optional<ValueAndEffectiveSource>>>> c = GETTER_CACHE.computeIfAbsent(destinationField, (fi) -> new ConcurrentHashMap<>());
        return c.computeIfAbsent(sourceClass, cl -> _sourceGetter(destinationClass, destinationField, sourceClass));

    }


    /**
     * Adds a {@link LeafMapper}.
     * @param instance the leaf mapper to add
     * @return A copy of the mapper, but with this one extra 'leaf mapper'.
     */
    public Mapper withLeafMapper(LeafMapper instance) {
        List<LeafMapper> list = new ArrayList<>(leafMappers);
        if (!list.contains(instance)) {
            list.add(instance);
            list.sort(Comparator.naturalOrder());
            return withLeafMappers(list);
        }
        return this;
    }

    /**
     * Specify a leaf mapper with a {@link Function} while mapping source and destination on type.
     * @param source Type of the source object
     * @param destination Type of the destination object
     * @param function Function specifying how to map source to destination
     * @return A new {@link Mapper}
     * @param <S> The source type
     * @param <D> The destination type
     * @see #withLeafMapper(LeafMapper)
     */
    public <S, D> Mapper withLeafMapper(Class<S> source, Class<D> destination, Function<S, D> function) {
        return withLeafMapper(new SimpleLeafMapper<>(source, destination) {
            @Override
            protected D map(S source) {
                return function.apply(source);
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
            return withLeafMapper(new EnumMapper(true, false))
                .withoutLeafMapper(new EnumMapper(false, false))
                .withLeafMapper(JaxbMapper.INSTANCE);
        } else {
            return withLeafMapper(new EnumMapper(false, false))
                .withoutLeafMapper(new EnumMapper(true, false))
                .withoutLeafMapper(JaxbMapper.INSTANCE);
        }
    }

    ///  PRIVATE METHODS

    /**
     * Helper method for {@link #map(Object, Object)}, recursively called for the class and superclass of the destination
     * object.
     */
    private void privateMap(Object source, Object destination, Class<?> forClass) {
        Class<?> sourceClass = source.getClass();
        Class<?> superClass = forClass.getSuperclass();
        if (superClass != null) {
            privateMap(source, destination, superClass);
        }
        for (Field f: forClass.getDeclaredFields()) {
            getAndSet(f, sourceClass, source, destination);
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
        Object destination) {
        Optional<Function<Object, Optional<ValueAndEffectiveSource>>> getter = sourceGetter(sourceClass, destinationField, destination.getClass());

        if (getter.isPresent()) {
            Optional<ValueAndEffectiveSource> value = getter.get().apply(source);
            value.ifPresentOrElse(v ->
                    destinationSetter(destination.getClass(), destinationField, sourceClass).accept(v.effectiveSource, destination, v.value),
                () -> log.debug("No field found for {} ({}) {}", destinationField.getName(), getAllSourceAnnotations(destinationField), sourceClass));
        } else {
            log.debug("Ignored destination field {} (No (matching) @Source annotation for {})", destinationField, sourceClass);
        }
    }


    private final Map<Field, Map<Class<?>, Optional<Function<Object, Optional<ValueAndEffectiveSource>>>>> GETTER_CACHE = new ConcurrentHashMap<>();



    /**
     * Uncached version of {@link #sourceGetter(Class, Field, Class)}
     */
    private Optional<Function<Object, Optional<ValueAndEffectiveSource>>> _sourceGetter(Class<?> destinationClass, Field destinationField, Class<?> sourceClass) {
        List<EffectiveSource> annotation = getAnnotation(sourceClass, destinationClass, destinationField);
        if (annotation.isEmpty()) {
            return Optional.empty();
        }
        boolean json = isJson(sourceClass);
        return Optional.of(o -> {
            for (EffectiveSource effectiveSource : annotation) {
                if (json) {
                    boolean subJson = !(effectiveSource.jsonPointer().equals(UNSET) && effectiveSource.jsonPath().equals(UNSET));
                    if (subJson) {
                        Function<Object, Optional<Object>> v = JsonUtil.valueFromJsonGetter(effectiveSource);
                        Optional<Object> value = v.apply(o);
                        if (value.isPresent()) {
                            return Optional.of(new ValueAndEffectiveSource(effectiveSource, value.get()));
                        }
                    }
                }
                Optional<Field> sourceField = getSourceField(sourceClass, effectiveSource.field());
                if (sourceField.isPresent()) {
                    final Field sf = sourceField.get();
                    Optional<Object> sourceValue = getSourceValue(o, sf, effectiveSource.path());
                    if (sourceValue.isPresent()) {
                        return Optional.of(new ValueAndEffectiveSource(effectiveSource, sourceValue.get()));
                    }
                }
            }
            return Optional.empty();
        });

    }

    private final Map<Class<?>, Map<Field, Map<Class<?>, TriConsumer<EffectiveSource, Object, Object>>>> SETTER_CACHE = new ConcurrentHashMap<>();

    /**
     * Returns a BiConsumer, that for a certain {@code destinationField} consumes a destination object, and sets a value
     * for the given field.
     * @param destinationField The field to set
     * @param sourceClass The currently matched class of the source object
     */
    private  TriConsumer<EffectiveSource, Object, Object> destinationSetter(Class<?> destinationClass, Field destinationField, Class<?> sourceClass) {
        Map<Field, Map<Class<?>, TriConsumer<EffectiveSource, Object, Object>>> classCache = SETTER_CACHE.computeIfAbsent(destinationClass, fi -> new ConcurrentHashMap<>());
        Map<Class<?>, TriConsumer<EffectiveSource, Object, Object>> cache = classCache.computeIfAbsent(destinationField, fi -> new ConcurrentHashMap<>());
        return cache.computeIfAbsent(sourceClass, c -> _destinationSetter(destinationClass, destinationField, c));
    }

    /**
     * Uncached version of {@link #destinationSetter(Class, Field, Class)}
     */
    private TriConsumer<EffectiveSource, Object, Object> _destinationSetter(Class<?> destinationClass, Field destinationField, Class<?> sourceClass) {
        List<EffectiveSource> annotation = getAnnotation(sourceClass, destinationClass, destinationField);

        if (isJson(sourceClass)) {
            destinationField.setAccessible(true);
            return (effectiveSource, destination, o) -> {
                try {
                    MappedField f = MappedField.of(destinationField, annotation);
                    destinationField.set(destination, mapLeaf(f, effectiveSource, o));
                } catch (Exception e) {
                    log.warn("When setting {} in {}: {}", o, destinationField, e.getMessage());
                }
            };
        }
        String sourceFieldName = UNSET;
        for (EffectiveSource es :  annotation) {
            if (! es.field().equals(UNSET)) {
                sourceFieldName =  es.field();
            } else {
                //if (sour)
            }
        }
        if (UNSET.equals(sourceFieldName)) {
            sourceFieldName = destinationField.getName();
        }
        Optional<Field> sourceField = getSourceField(sourceClass, sourceFieldName);
        if (sourceField.isPresent()) {
            destinationField.setAccessible(true);
            return (effectiveSource, destination, o) -> {
                try {
                    MappedField f = MappedField.of(destinationField, annotation);
                    Object convertedValue = mapLeaf(f, effectiveSource, o);
                    destinationField.set(destination, convertedValue);
                } catch (Exception e) {
                    log.warn("When setting '{}' in {}: {} (because {})", o, destinationField, e.getMessage(), annotation);
                }
            };
        }
        return Consumers.triNop();
    }


    public Object mapLeaf(MappedField destinationField, EffectiveSource effectiveSource, Object o) {

        for (LeafMapper lm : effectiveSource.leafMappers()) {
            LeafMapper.Leaf result = lm.map(this, effectiveSource, destinationField, o);
            if (result.success()) {
                o = result.result();
                if (result.terminate()) {
                    return o;
                }
            }
        }
        for (LeafMapper leafMapper : leafMappers) {
            LeafMapper.Leaf result = leafMapper.map(this, effectiveSource, destinationField, o);
            if (result.success()) {
                o = result.result();
                if (result.terminate()) {
                    break;
                }
            }
        }
        return o;
    }


    public record ValueAndEffectiveSource(EffectiveSource effectiveSource, Object value) {}


}
