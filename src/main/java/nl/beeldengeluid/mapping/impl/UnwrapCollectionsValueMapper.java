package nl.beeldengeluid.mapping.impl;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import nl.beeldengeluid.mapping.*;
import nl.beeldengeluid.mapping.ValueMapper;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;

import static nl.beeldengeluid.mapping.Mapper.current;

@Slf4j
@EqualsAndHashCode
public class UnwrapCollectionsValueMapper implements ValueMapper<Object> {

    private UnwrapCollectionsValueMapper() {

    }

    public static UnwrapCollectionsValueMapper INSTANCE = new UnwrapCollectionsValueMapper();

    @Override
    public ValueMap mapValue(Mapper mapper, MappedField destinationField, Object possiblyACollection) {
        if (possiblyACollection instanceof Collection<?> list) {
            if (destinationField.type() == List.class) {
                ParameterizedType genericType = (ParameterizedType) destinationField.type();
                Class<?> genericClass = (Class<?>) genericType.getActualTypeArguments()[0];
                if (genericClass != Object.class) {
                    return ValueMapper.mapped(list.stream()
                        .map(o -> {
                                try {
                                    Object mapped = mapper.mapValue(genericClass, destinationField, o);
                                    return mapped;
                                } catch (MapException me) {
                                    log.warn(me.getMessage(), me);
                                    return null;
                                }
                            }
                        ).toList());
                }
            }

        }
        return ValueMapper.NOT_MAPPED;
    }
}
