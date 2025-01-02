package nl.beeldengeluid.mapping.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Optional;

import nl.beeldengeluid.mapping.EffectiveSource;
import nl.beeldengeluid.mapping.MappedField;


/**
 * @see MappedField
 */
public class ReflectMappedField implements MappedField {

    private final Field field;
    private final EffectiveSource source;

    public ReflectMappedField(Field field, EffectiveSource effectiveSource) {
        this.field = field;
        this.source = effectiveSource;
    }

    @Override
    public String name() {
        return field.getName();
    }

    @Override
    public Type genericType() {
        return field.getGenericType();
    }

    @Override
    public Optional<EffectiveSource> source() {
        return Optional.ofNullable(source);
    }

    @Override
    public <T extends Annotation> T annotation(Class<T> annotation) {
        return field.getAnnotation(annotation);
    }


}
