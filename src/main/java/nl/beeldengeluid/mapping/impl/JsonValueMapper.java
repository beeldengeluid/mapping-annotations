package nl.beeldengeluid.mapping.impl;

import nl.beeldengeluid.mapping.Mapper;
import nl.beeldengeluid.mapping.ValueMapper;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public class JsonValueMapper implements ValueMapper {


    private static Object considerJson(Mapper mapper, Object o, Field destinationField, Class<?> destinationClass) {
        List<BiFunction<Object, Field, Optional<Object>>> customMappers = mapper.customMappers().get(destinationClass);
        if (customMappers != null) {
            for (BiFunction<Object, Field, Optional<Object>> customMapper: customMappers){
                Optional<Object> tryMap = customMapper.apply(o, destinationField);
                if (tryMap.isPresent()) {
                    o = tryMap.get();
                }
            }
        }
        return o;
    }
    @Override
    public ValueMap mapValue(Class<?> destinationClass, Field destinationField, Object o) {
        return null;
    }
}
