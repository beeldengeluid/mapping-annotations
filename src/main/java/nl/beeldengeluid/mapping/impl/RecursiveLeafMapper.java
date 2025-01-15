package nl.beeldengeluid.mapping.impl;

import nl.beeldengeluid.mapping.*;

/**
 * A leaf mapper that actually calls the {@link Mapper} again on the leaf.
 */
public class RecursiveLeafMapper implements LeafMapper {

    public static final RecursiveLeafMapper INSTANCE = new RecursiveLeafMapper();

    private RecursiveLeafMapper() {

    }

    @Override
    public Leaf map(Mapper mapper, EffectiveSource effectiveSource,  MappedField destinationField, Object o) {
        if (!mapper.canMap(o, destinationField.type())) {
            return LeafMapper.NOT_MAPPED;
        }
        Object mapped = mapper.newInstance(destinationField.type());
        mapper.subMap(o, mapped,destinationField.type());

        return LeafMapper.mapped(mapped);

    }

    @Override
    public int weight() {
        return 1000;
    }
}
