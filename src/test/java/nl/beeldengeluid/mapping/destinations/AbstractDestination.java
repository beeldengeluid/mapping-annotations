package nl.beeldengeluid.mapping.destinations;

import lombok.Data;

import nl.beeldengeluid.mapping.annotations.Source;


/**
 * A base class so we test inheritance
 */
@Data
public abstract class AbstractDestination {


    @Source(jsonPointer ="/title")
    String title;
}
