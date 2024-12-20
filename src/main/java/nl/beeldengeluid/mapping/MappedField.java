package nl.beeldengeluid.mapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

public record MappedField(String name, Type type, Annotation[] annotations)  {


    static MappedField of(Field field) {
        return new MappedField(field.getName(), field.getGenericType(), field.getAnnotations());
    }
}
