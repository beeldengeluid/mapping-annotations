package nl.beeldengeluid.mapping.destinations;

import nl.beeldengeluid.mapping.annotations.Source;


/**
 * Can a destination  be a record?
 */
@lombok.Builder // yes, if you provide a builder
public record DestinationRecord(
    @Source
    String title
) {
}
