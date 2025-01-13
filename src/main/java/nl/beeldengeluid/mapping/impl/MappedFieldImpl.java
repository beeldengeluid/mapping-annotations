package nl.beeldengeluid.mapping.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import java.util.List;
import java.util.Optional;

import nl.beeldengeluid.mapping.EffectiveSource;
import nl.beeldengeluid.mapping.MappedField;

/**
 * @see MappedField
 * @param name
 * @param genericType
 * @param effectiveSource
 */
public record MappedFieldImpl(String name, Type genericType,  List<EffectiveSource> effectiveSource) implements MappedField {


    @Override
    public <T extends Annotation> T annotation(Class<T> annotation) {
        return null;
    }

    @Override
    public List<EffectiveSource> source() {
        return effectiveSource;
    }
}
