package nl.beeldengeluid.mapping.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import nl.beeldengeluid.mapping.MappedField;

public record MappedFieldImpl(String name, Type genericType) implements MappedField {


    @Override
    public <T extends Annotation> T annotation(Class<T> annotation) {
        return null;
    }
}
