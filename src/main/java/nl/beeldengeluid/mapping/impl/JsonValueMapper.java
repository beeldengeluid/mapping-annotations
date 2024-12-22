package nl.beeldengeluid.mapping.impl;

import nl.beeldengeluid.mapping.*;

public class JsonValueMapper implements LeafMapper {

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
    public Leaf map(Mapper mapper, MappedField field, Object o) {
        return null;
    }
}
