package nl.beeldengeluid.mapping.destinations;

import lombok.Getter;
import lombok.Setter;
import nl.beeldengeluid.mapping.annotations.Source;

/**
 * A destination class with defines that there is a 'json' field where other sources may take from
 */
@Getter@Setter
@Source(field = "json")
public class FromJsonFieldDestination extends  AbstractDestination {

    public FromJsonFieldDestination() {

    }


    @Source(jsonPointer ="/description")
    String description;


}
