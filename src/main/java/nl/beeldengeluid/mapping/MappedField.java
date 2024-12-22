package nl.beeldengeluid.mapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Optional;

import nl.beeldengeluid.mapping.impl.ReflectMappedField;

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


    Optional<EffectiveSource> source();

    //<T extends Annotation>  T annotation(Class<T> annotation);

    static MappedField of (Field field, EffectiveSource source){
        return new ReflectMappedField(field, source);
    }

}
