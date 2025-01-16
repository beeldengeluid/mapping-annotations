package nl.beeldengeluid.mapping.impl;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import nl.beeldengeluid.mapping.*;


/**
 * This can be used on a leaf that is not a {@link com.fasterxml.jackson.databind.JsonNode} by itself, but easily convertible to it. E.g. a {@code String} containing json.
 * <p>
 * This can also be done by adding {@code jsonPointer=""} to the {@code Source} annotation.
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
