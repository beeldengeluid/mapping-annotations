package nl.beeldengeluid.mapping;

import nl.beeldengeluid.mapping.annotations.Source;

@lombok.Builder
public record DestinationRecord(
    @Source
    String title
) {
}
