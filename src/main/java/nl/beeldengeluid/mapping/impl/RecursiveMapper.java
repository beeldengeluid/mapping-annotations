package nl.beeldengeluid.mapping.impl;

import nl.beeldengeluid.mapping.*;

public class RecursiveMapper implements LeafMapper {

    public static final RecursiveMapper INSTANCE = new RecursiveMapper();

    private RecursiveMapper() {

    }

    @Override
    public Leaf map(Mapper mapper, MappedField destinationField, Object o) {
        if (!mapper.canMap(o, destinationField.type())) {
            return LeafMapper.NOT_MAPPED;
        }
        Object mapped = mapper.newInstance(destinationField.type());
        mapper.subMap(o, mapped,destinationField.type());

        return LeafMapper.mapped(mapped);

    }

    public int weight() {
        return 100;
    }
}