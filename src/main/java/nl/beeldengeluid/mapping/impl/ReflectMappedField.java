package nl.beeldengeluid.mapping.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

import nl.beeldengeluid.mapping.MappedField;

public class ReflectMappedField implements MappedField {

    private final Field field;

    public ReflectMappedField(Field field) {
        this.field = field;
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
    public <T extends Annotation> T annotation(Class<T> annotation) {
        return field.getAnnotation(annotation);
    }
}
