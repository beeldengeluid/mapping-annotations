package nl.beeldengeluid.mapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;

import nl.beeldengeluid.mapping.impl.ReflectMappedField;

/**
 * Representation of a mapped field. The most basic implementation is {@link ReflectMappedField} which just wraps an actual {@link Field}.
 * But sometimes a field is kind of 'virtual', e.g. entries in a collection. In that case {@link nl.beeldengeluid.mapping.impl.MappedFieldImpl} is used.
 */
public interface MappedField {

    String name();

    Type genericType();

    default Class<?> type() {
        if (genericType() instanceof ParameterizedType pt) {
            return (Class<?>) pt.getRawType();
        } else if (genericType() instanceof Class<?> c) {
            return c;
        } else {
            throw new IllegalStateException();
        }
    }

    <T extends Annotation>  T annotation(Class<T> annotation);


    EffectiveSource source();

    //<T extends Annotation>  T annotation(Class<T> annotation);

    static MappedField of (Field field, EffectiveSource source){
        return new ReflectMappedField(field, source);
    }

}
