/*
 * Copyright (C) 2024 Licensed under the Apache License, Version 2.0
 */
package nl.beeldengeluid.mapping;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;

import nl.beeldengeluid.mapping.impl.EffectiveSource;
import nl.beeldengeluid.mapping.impl.JsonUtil;
import nl.beeldengeluid.mapping.impl.ValueMapper;
import org.meeuw.functional.Functions;
import nl.beeldengeluid.mapping.annotations.Source;


import com.fasterxml.jackson.databind.JsonNode;

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
@lombok.Builder
public class Mapper {

    /**
     * The default {@link Mapper} instance. Mappers are stateless, but can contain some configuration.
     */
    public static final Mapper MAPPER = Mapper.builder().build();

    /**
     * The Mapper currently effective. A {@link ThreadLocal}. Defaults to {@link #MAPPER}
     */
    private static final ThreadLocal<Mapper> CURRENT = ThreadLocal.withInitial(() -> MAPPER);

    public static Mapper current() {
        return CURRENT.get();
    }

    @With
    @Getter
    private final boolean clearsJsonCacheEveryTime;

    @With
    @lombok.Builder.Default
    @Getter
    private final boolean supportsJaxbAnnotations = true;

    @With(AccessLevel.PACKAGE)
    @lombok.Builder.Default
    @Getter
    private final Map<Class<?>, List<BiFunction<Object, Field, Optional<Object>>>> customMappers = Collections.emptyMap();


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
        try {
            T destination = destinationClass.getDeclaredConstructor().newInstance();
            map(source, destination, groups);
            return destination;
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


    /**
     * Adds a custom mapping from a {@link JsonNode} to {@code destinationClass}. This is
     * a convenience version of {@link #withCustomMapper(Class, Class, Function)}, where the first argument is {@link JsonNode}{@code .class}
     * @param destinationClass The target class
     * @param mapper A {@link BiFunction} that accepts an object of type {@code SourceClass} and a {@link Field} in the destination where it is for.
     * <p>
     * @see #withCustomJsonMapper(Class, Function)
     */
    public <D> Mapper withCustomJsonMapper(Class<D> destinationClass,  BiFunction<JsonNode, Field, Optional<D>> mapper) {
        return withCustomMapper(JsonNode.class, destinationClass, mapper);
    }

    /**
     * Adds a custom mapping from a {@link JsonNode} to {@code destinationClass}. A convenience version
     * of {@link #withCustomJsonMapper(Class, BiFunction)} which just accepts @ {@link Function} rather than a {@link BiFunction} because the second argument of the BiFunction can often just be ignored.
     *
     * @param destinationClass The target class
     * @param mapper A {@link Function} that accepts an object of type {@code SourceClass} and produces an (optional of) the desired type.
     * @return A new mapper with the custom mapper added.
     */
    public <D> Mapper withCustomJsonMapper(Class<D> destinationClass,  Function<JsonNode, Optional<D>> mapper) {
        return withCustomJsonMapper(destinationClass, Functions.ignoreArg2(mapper));
    }

    /**
     * Adds a custom mapping from {@code sourceClass} to {@code destinationClass}
     * @param sourceClass The expected source type
     * @param destinationClass The target class
     * @param mapper A {@link BiFunction} that accepts an object of type {@code SourceClass} and a {@link Field} in the destination where it is for.
     * @see #withCustomMapper(Class, Class, Function)
     * @see #withCustomJsonMapper(Class, BiFunction)
     * @return A new mapper with the custom mapper added.
     */
    public <S, D> Mapper withCustomMapper(Class<S> sourceClass, Class<D> destinationClass, BiFunction<S, Field, Optional<D>> mapper) {
        var current = new HashMap<>(customMappers());
        var list = current.get(destinationClass);
        List<BiFunction<Object, Field, Optional<Object>>> newList = list == null ? new ArrayList<>() : new ArrayList<>(list);
        newList.add((o, f) -> mapper.apply( (S) o, f).map(d -> d));
        current.put(destinationClass, Collections.unmodifiableList(newList));
        return withCustomMappers(Collections.unmodifiableMap(current));
    }

    /**
     * Adds a custom mapping from {@code sourceClass} to {@code destinationClass}. A convenience version
     * of {@link #withCustomMapper(Class, Class, BiFunction)} which just accepts @ {@link Function} rather than a {@link BiFunction} because the second argument of the BiFunction can often just be ignored.
     * @param sourceClass The expected source type
     * @param destinationClass The target class
     * @param mapper A {@link Function} that accepts an object of type {@code SourceClass}
     * @see #withCustomJsonMapper(Class, BiFunction) (Class, Function) For the common case where the source object is json
     * @return A new mapper with the custom mapper added.
     */
    public <S, D> Mapper withCustomMapper(Class<S> sourceClass, Class<D> destinationClass, Function<S, Optional<D>> mapper) {
        return withCustomMapper(sourceClass, destinationClass, Functions.ignoreArg2(mapper));
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
                        destinationField.set(destination, ValueMapper.valueFor(this, destinationField, destinationClass,o));
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
                        Object convertedValue = ValueMapper.valueFor(this, destinationField, destinationField.getType(), o);
                        destinationField.set(destination, convertedValue);
                    } catch (Exception e) {
                        log.warn("When setting '{}' in {}: {}", o, destinationField, e.getMessage());
                    }
                };
            }
        }
        return (d, v) -> {};
    }







}