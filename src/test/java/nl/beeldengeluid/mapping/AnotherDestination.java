package nl.beeldengeluid.mapping;

import lombok.Getter;
import lombok.Setter;
import nl.beeldengeluid.mapping.annotations.Source;

@Getter@Setter
@Source(field = "json")
public class AnotherDestination extends  AbstractDestination {

    public AnotherDestination() {

    }


    @Source(jsonPointer ="/description")
    String description;


}
