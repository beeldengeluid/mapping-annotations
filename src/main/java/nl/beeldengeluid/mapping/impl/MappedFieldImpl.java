package nl.beeldengeluid.mapping.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import java.util.Optional;

import nl.beeldengeluid.mapping.EffectiveSource;
import nl.beeldengeluid.mapping.MappedField;

public record MappedFieldImpl(String name, Type genericType, EffectiveSource s) implements MappedField {


    @Override
    public <T extends Annotation> T annotation(Class<T> annotation) {
        return null;
    }

    @Override
    public Optional<EffectiveSource> source() {
        return Optional.ofNullable(s);
    }
}
