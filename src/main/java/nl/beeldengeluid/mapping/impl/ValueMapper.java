package nl.beeldengeluid.mapping.impl;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import nl.beeldengeluid.mapping.MapException;
import nl.beeldengeluid.mapping.Mapper;

import static nl.beeldengeluid.mapping.Mapper.current;

@Slf4j
public class ValueMapper {



    public static  Object valueFor(Mapper mapper,  Field destinationField, Class<?> destinationClass,  Object o) throws ReflectiveOperationException {

        //o = considerJson(mapper, o, destinationField, destinationClass);
        return o;
    }




 

    /**
     *
     */
    @SuppressWarnings({"ReassignedVariable", "unchecked"})
    public static <T> T subMap(Mapper mapper, Object source, Class<T> destinationClass, Field destinationField, Class<?>... groups)  {

        try {
            source = ValueMapper.valueFor(mapper, destinationField, destinationClass, source);
            if (destinationClass.isInstance(source)) {
                return (T) source;
            }
            T destination = destinationClass.getDeclaredConstructor().newInstance();
            mapper.subMap(source, destination,  destinationClass, groups);
            return destination;
        } catch (ReflectiveOperationException e) {
            throw new MapException(e);
        }


    }

}
