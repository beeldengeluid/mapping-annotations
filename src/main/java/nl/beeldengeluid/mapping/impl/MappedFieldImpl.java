package nl.beeldengeluid.mapping.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import nl.beeldengeluid.mapping.EffectiveSource;
import nl.beeldengeluid.mapping.MappedField;

/**
 * @see MappedField
 * @param name
 * @param genericType
 * @param source Effective Source
 */
public record MappedFieldImpl(String name, Type genericType,  EffectiveSource source) implements MappedField {


    @Override
    public <T extends Annotation> T annotation(Class<T> annotation) {
        return null;
    }

}
