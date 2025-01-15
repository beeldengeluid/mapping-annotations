package nl.beeldengeluid.mapping.impl;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import nl.beeldengeluid.mapping.*;


/**
 * If a leaf contains a {@link Collection} this {@link LeafMapper} will call the mapper for all of <em>its</em> elements.
 */
@Slf4j
@EqualsAndHashCode
public class UnwrapCollectionsLeafMapper implements LeafMapper {

    public static final UnwrapCollectionsLeafMapper INSTANCE = new UnwrapCollectionsLeafMapper();

    private UnwrapCollectionsLeafMapper() {
        // singleton
    }

    protected Collector<Object, ?, ?> getCollector(MappedField field) {
         if (field.type() == List.class) {
             return Collectors.toList();
         } else {
             throw new UnsupportedOperationException("UnwrapCollectionsLeafMapper does (yet) not support collections of type " + field.type());
         }
    }

    @Override
    public Leaf map(Mapper mapper, EffectiveSource effectiveSource, MappedField destinationField, Object possiblyACollection) {
        if (possiblyACollection instanceof Collection<?> collection) {
            ParameterizedType genericType = (ParameterizedType) destinationField.genericType();
            Class<?> genericClass = (Class<?>) genericType.getActualTypeArguments()[0];
            if (genericClass != Object.class) {
                return LeafMapper.mapped(collection.stream()
                    .map(o -> {
                            try {
                                var m = new MappedFieldImpl(destinationField.name(),
                                    genericClass,
                                    effectiveSource
                                );
                                return mapper.mapLeaf(m, effectiveSource, o).orElse(o);
                            } catch (MapException me) {
                                log.warn(me.getMessage(), me);
                                return LeafMapper.NOT_MAPPED;
                            }
                        }
                    ).collect(getCollector(destinationField)));
            }

        }
        return LeafMapper.NOT_MAPPED;
    }
}
