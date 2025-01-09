package nl.beeldengeluid.mapping.impl;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import nl.beeldengeluid.mapping.*;


/**
 */
@Getter
@EqualsAndHashCode
public class PrimitiveMapper implements LeafMapper {

    public static final PrimitiveMapper INSTANCE = new PrimitiveMapper();



    private PrimitiveMapper() {
    }

    @Override
    public Leaf map(Mapper mapper, MappedField destinationField, Object o) {
        if (CharSequence.class.isAssignableFrom(destinationField.type()) && ! (o instanceof CharSequence)) {
            return LeafMapper.mapped(o.toString());
        }
        if (Long.class.isAssignableFrom(destinationField.type())) {
            if (o instanceof CharSequence string) {
                return LeafMapper.mapped(Long.parseLong(o.toString()));
            }
        }
        return LeafMapper.NOT_MAPPED;
    }
}
