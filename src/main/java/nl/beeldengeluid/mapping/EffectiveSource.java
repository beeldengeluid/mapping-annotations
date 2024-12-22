package nl.beeldengeluid.mapping;

import java.util.Arrays;

import nl.beeldengeluid.mapping.annotations.Source;

@lombok.Builder
public record EffectiveSource(
    Class<?> sourceClass,

    String jsonPath,

    String jsonPointer,

    String field,

    String[] path,

    Class<?>[] groups
) {

    @Source
    private static class DefaultHolder { }
    static Source DEFAULTS = DefaultHolder.class.getAnnotation(Source.class);

    public static EffectiveSource of(Source source, Source defaults) {
        if (defaults == null) {
            defaults = DEFAULTS;
        }
        var builder = builder();

        if (DEFAULTS.sourceClass().equals(source.sourceClass())) {
            builder.sourceClass(defaults.sourceClass());
        } else {
            builder.sourceClass(source.sourceClass());
        }
        if (DEFAULTS.jsonPath().equals(source.jsonPath())) {
            builder.jsonPath(defaults.jsonPath());
        } else {
            builder.jsonPath(source.jsonPath());
        }
        if (DEFAULTS.jsonPointer().equals(source.jsonPointer())) {
            builder.jsonPointer(defaults.jsonPointer());
        } else {
            builder.jsonPointer(source.jsonPointer());
        }

        if (DEFAULTS.field().equals(source.field())) {
            builder.field(defaults.field());
        } else {
            builder.field(source.field());
        }

         if (Arrays.equals(DEFAULTS.path(), source.path())) {
             builder.path(defaults.path());
         } else {
             builder.path(source.path());
         }

        if (Arrays.equals(DEFAULTS.groups(), source.groups())) {
            builder.groups(defaults.groups());
         } else {
             builder.groups(source.groups());
         }
/*
        if (Arrays.equals(DEFAULTS.customMappers(), source.customMappers())) {
            builder.customMappers(defaults.customMappers());
         } else {
             builder.customMappers(source.customMappers());
         }*/
         return builder.build();
    }
}
