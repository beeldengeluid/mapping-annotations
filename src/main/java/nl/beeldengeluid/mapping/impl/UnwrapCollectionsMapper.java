package nl.beeldengeluid.mapping.impl;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.util.*;

import nl.beeldengeluid.mapping.*;


/**
 * If a leaf contains a {@link Collection} this {@link LeafMapper} will call the mapper for all of <em>its</em> elements.
 */
@Slf4j
@EqualsAndHashCode
public class UnwrapCollectionsMapper implements LeafMapper {

    public static final UnwrapCollectionsMapper INSTANCE = new UnwrapCollectionsMapper();

    private UnwrapCollectionsMapper() {
        // singleton
    }

    @Override
    public Leaf map(Mapper mapper, EffectiveSource effectiveSource, MappedField destinationField, Object possiblyACollection) {
        if (possiblyACollection instanceof Collection<?> list) {
            if (destinationField.type() == List.class) {
                ParameterizedType genericType = (ParameterizedType) destinationField.genericType();
                Class<?> genericClass = (Class<?>) genericType.getActualTypeArguments()[0];
                if (genericClass != Object.class) {
                    return LeafMapper.mapped(list.stream()
                        .map(o -> {
                                try {
                                    var m = new MappedFieldImpl(destinationField.name(),
                                        genericClass,
                                        List.of(effectiveSource)
                                    );
                                    Object mapped = mapper.mapLeaf(m, effectiveSource, o);
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
        return LeafMapper.NOT_MAPPED;
    }
}
