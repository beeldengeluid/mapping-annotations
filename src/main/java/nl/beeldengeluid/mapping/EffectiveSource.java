package nl.beeldengeluid.mapping;

import java.util.Arrays;

import nl.beeldengeluid.mapping.annotations.Source;


/**
 * {@code Source} annotations can be on class and on field level. This 'EffectiveSource' contains the effective value of all attributes.
 *
 * @param sourceClass See {@link Source#sourceClass()}
 * @param jsonPath See {@link Source#jsonPath()}
 * @param jsonPointer See {@link Source#jsonPointer()}
 * @param field See {@link Source#field()}
 * @param path See {@link Source#path()}
 * @param leafMappers See {@link Source#leafMappers()}
 */
@lombok.Builder
public record EffectiveSource(
    Class<?> sourceClass,

    String jsonPath,

    String jsonPointer,

    String field,

    String[] path,

    Class<? extends LeafMapper>[] leafMappers

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

        if (DEFAULTS.leafMappers().equals(source.leafMappers())) {
            builder.leafMappers(defaults.leafMappers());
        } else {
            builder.leafMappers(source.leafMappers());
        }

         return builder.build();
    }
}
