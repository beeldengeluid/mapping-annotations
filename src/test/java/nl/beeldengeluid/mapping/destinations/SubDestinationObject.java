package nl.beeldengeluid.mapping.destinations;

import lombok.*;

import com.fasterxml.jackson.databind.JsonNode;

import nl.beeldengeluid.mapping.annotations.Source;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubDestinationObject {

    @Source(sourceClass = JsonNode.class, jsonPointer = "/currentbroadcaster.broadcaster/resolved_value")
    String broadcaster;

    @Source(sourceClass = JsonNode.class, jsonPointer = "/resolved_value")
    String broadcaster2;

    long id;
}
