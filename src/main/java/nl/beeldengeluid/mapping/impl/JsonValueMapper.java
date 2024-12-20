package nl.beeldengeluid.mapping.impl;

import com.fasterxml.jackson.databind.JsonNode;

import nl.beeldengeluid.mapping.ValueMapper;
import nl.beeldengeluid.mapping.*;

public class JsonValueMapper implements ValueMapper<JsonNode> {

      /*
    private static Object considerJson(Mapper mapper, Object o, Field destinationField, Class<?> destinationClass) {

        List<BiFunction<Object, Field, Optional<Object>>> customMappers = mapper.customMappers().get(destinationClass);
        if (customMappers != null) {
            for (BiFunction<Object, Field, Optional<Object>> customMapper : customMappers) {
                Optional<Object> tryMap = customMapper.apply(o, destinationField);
                if (tryMap.isPresent()) {
                    o = tryMap.get();
                }
            }
        }
        return o;
    }
    */

    @Override
    public ValueMap mapValue(Mapper mapper, MappedField field, JsonNode o) {
        return null;
    }
}
