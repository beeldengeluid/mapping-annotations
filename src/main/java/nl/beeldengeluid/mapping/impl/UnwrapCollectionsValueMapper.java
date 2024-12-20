package nl.beeldengeluid.mapping.impl;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;

import nl.beeldengeluid.mapping.ValueMapper;
import nl.beeldengeluid.mapping.*;

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
                ParameterizedType genericType = (ParameterizedType) destinationField.genericType();
                Class<?> genericClass = (Class<?>) genericType.getActualTypeArguments()[0];
                if (genericClass != Object.class) {
                    return ValueMapper.mapped(list.stream()
                        .map(o -> {
                                try {
                                    MappedField field = new MappedFieldImpl(destinationField.name(), genericClass);
                                    Object mapped = mapper.mapValue(field, o);
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
