/*
 * Copyright (C) 2024 Licensed under the Apache License, Version 2.0
 */
package nl.beeldengeluid.mapping.impl;

import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.JsonNode;

import nl.beeldengeluid.mapping.EffectiveSource;
import nl.beeldengeluid.mapping.Mapper;
import nl.beeldengeluid.mapping.annotations.Source;
import nl.beeldengeluid.mapping.annotations.Sources;

import static nl.beeldengeluid.mapping.annotations.Source.UNSET;

/**
 * Contains methods performing the reflection and caching needed to implement {@link Mapper}, and {@link Source} annotations
 *
 * @author Michiel Meeuwissen
 * @since 0.1
 */
@Slf4j
public class Util {

    private Util() {
        // no instances allowed
    }


    public static List<EffectiveSource> getAnnotation(Class<?> sourceClass, Class<?> destinationClass, Field destinationField) {

        destinationField =  associatedBuilderField(destinationField).orElse(destinationField);
        Source defaultValues = null;
        {
            Class<?> clazz = destinationClass;
            while(clazz != Object.class && defaultValues == null) {
                defaultValues = clazz.getAnnotation(Source.class);
                clazz = clazz.getSuperclass();
            }
        }
        List<EffectiveSource> list = new ArrayList<>();
        for (Source annotation : getAllSourceAnnotations(destinationField)) {
            EffectiveSource proposal =  EffectiveSource.of(annotation, defaultValues);
            if (proposal.field().equals(UNSET)) {
                log.debug("No source field set for {} {}. May default to {}", destinationField, proposal, destinationField.getName());
            }
            if (matches(proposal, sourceClass, destinationField.getName())) {
                list.add(proposal);
            } else {
                log.debug("Not matching {}", proposal);
            }
        }

        return Collections.unmodifiableList(list);
    }

    private static List<Source> getAllSourceAnnotations(AnnotatedElement element) {
        List<Source> result = new ArrayList<>();
        getAllSourceAnnotations(element, new HashSet<>(), result);
        return result;
    }
    private static void getAllSourceAnnotations(AnnotatedElement element, Set<AnnotatedElement> delt, List<Source> result) {
        if (delt.add(element)) {
            for (Annotation annotation : element.getAnnotations()) {
                if (annotation instanceof  Source s) {
                    result.add(s);
                } else if (annotation instanceof  Sources sources) {
                    result.addAll(Arrays.asList(sources.value()));
                } else {
                    getAllSourceAnnotations(annotation.annotationType(), delt, result);
                }
            }
        }
    }


    private static Optional<Field> associatedBuilderField(Field f) {
        if (f != null && f.getAnnotations().length == 0) {
            Class<?> clazz = f.getDeclaringClass();
            if (clazz.getName().endsWith("Builder")) {
                try {
                    Method build = clazz.getDeclaredMethod("build");
                    if (Modifier.isPublic(build.getModifiers()) && ! Modifier.isStatic(build.getModifiers())) {
                        Class<?> targetClass = build.getReturnType();
                        Field buildField = targetClass.getDeclaredField(f.getName());
                        return Optional.of(buildField);
                    }
                } catch (NoSuchMethodException | NoSuchFieldException e) {
                    log.warn(e.getMessage(), e);
                }
            }
        }
        return Optional.empty();
    }

    public static boolean isJson(Class<?> clazz) {
        return JsonNode.class.isAssignableFrom(clazz);

    }


    private static boolean matches(EffectiveSource source, Class<?> sourceClass, String destinationField, Class<?>... groups) {
        if (source == null) {
            return false;
        }
        String field = source.field();

        if (UNSET.equals(field)) {
            if (isJson(sourceClass))  {
                return true;
            } else {
                field = destinationField;
            }
        }
        return source.sourceClass().isAssignableFrom(sourceClass) &&
            getSourceField(sourceClass, field).isPresent();

    }

    // caches make test in MapperTest about 10 times as fast.
    private static final Map<Class<?>, Map<String, Optional<Field>>> cache = new ConcurrentHashMap<>();

    public static Optional<Field> getSourceField(final Class<?> sourceClass, String sourceField) {
        // to disable cache and measure its effect
        //return _getSourceField(sourceClass, sourceField);
        Map<String, Optional<Field>> c =  cache.computeIfAbsent(sourceClass, cl -> new ConcurrentHashMap<>());

        return c.computeIfAbsent(sourceField, f -> _getSourceField(sourceClass, f));
    }

    private static Optional<Field> _getSourceField(final Class<?> sourceClass, String sourceField) {
         Class<?> clazz = sourceClass;

            while (clazz != null) {
                try {
                    Field declaredField = clazz.getDeclaredField(sourceField);
                    declaredField.setAccessible(true);
                    return Optional.of(declaredField);
                } catch (NoSuchFieldException ignored) {

                }
                clazz = clazz.getSuperclass();
            }
            log.debug("No source field {} found for {}", sourceField, sourceClass);
            return Optional.empty();
    }

    public static Optional<Object> getSourceValue(Object source, String sourceField, List<String> path) {
         return getSourceField(source.getClass(), sourceField)
             .flatMap(f -> getSourceValue(source, f, path));
    }

    public static Optional<Object> getSourceValue(Object source, Field sourceField, List<String> path) {
          try {
              sourceField.setAccessible(true);
              Object value = sourceField.get(source);
              for (String p : path) {
                  if (value != null) {
                      Field su = value.getClass().getDeclaredField(p);
                      su.setAccessible(true);
                      value = su.get(value);
                  }
              }
              return Optional.ofNullable(value);
          } catch (IllegalAccessException | NoSuchFieldException  | IllegalArgumentException e) {
              log.warn(e.getMessage());
              return Optional.empty();
          }

    }





}
