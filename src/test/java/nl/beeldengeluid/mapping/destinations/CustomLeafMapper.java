package nl.beeldengeluid.mapping.destinations;

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

import nl.beeldengeluid.mapping.*;

public class CustomLeafMapper extends SimplerLeafMapper<JsonNode, String> {

    public static final CustomLeafMapper INSTANCE = new CustomLeafMapper();

    protected CustomLeafMapper() {
        super(JsonNode.class, String.class);
    }

    @Override
    protected String map(JsonNode source) {
        return Optional.ofNullable(source.get("custom")).map(c -> "{{" + c.textValue() + "}}").orElse(null);
    }
}
