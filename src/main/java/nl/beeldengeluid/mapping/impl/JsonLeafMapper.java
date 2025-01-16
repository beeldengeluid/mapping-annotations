package nl.beeldengeluid.mapping.impl;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import nl.beeldengeluid.mapping.*;


/**
 *
 */
@Getter
@EqualsAndHashCode
public class JsonLeafMapper implements LeafMapper {

    public static final JsonLeafMapper INSTANCE = new JsonLeafMapper();

    private JsonLeafMapper() {
    }

    @Override
    public Leaf map(Mapper mapper, EffectiveSource effectiveSource,  MappedField destinationField, Object o) {
        try {
            return LeafMapper.mapped(JsonUtil.getJson(o));
        } catch (Exception e) {
            return NOT_MAPPED;
        }
    }
}
