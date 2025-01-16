package nl.beeldengeluid.mapping.destinations;

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

import nl.beeldengeluid.mapping.*;

public class CustomLeafMapper implements LeafMapper {

    public static final CustomLeafMapper INSTANCE = new CustomLeafMapper();

    protected CustomLeafMapper() {
            }

    @Override
    public Leaf map(Mapper mapper, EffectiveSource effectiveSource, MappedField destinationField, Object o) {

        if (o instanceof JsonNode n) {
            Optional<String> m = map(effectiveSource, n);
            if (m.isPresent()) {
                return LeafMapper.mapped(m.get());
            }
        }
        return NOT_MAPPED;

    }

    //@Override
    protected Optional<String> map(EffectiveSource effectiveSource, JsonNode source) {
        return Optional.ofNullable(source.get("custom")).map(c -> "{{" + c.textValue() + "}}");
    }
}
