package nl.beeldengeluid.mapping.destinations;

import lombok.Getter;

import com.fasterxml.jackson.databind.JsonNode;

import nl.beeldengeluid.mapping.annotations.Source;

@Source(sourceClass = JsonNode.class)
@Getter
public class SubObject {

    @Source(jsonPointer = "/x")
    String a;
    @Source(jsonPointer = "/y")
    String b;
}
