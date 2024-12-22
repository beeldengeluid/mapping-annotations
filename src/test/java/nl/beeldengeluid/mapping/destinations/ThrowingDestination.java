package nl.beeldengeluid.mapping.destinations;

import lombok.Getter;
import lombok.Setter;

/**
 * A destination that's hare to instantiate because its single constructor just throws.
 */
@Getter@Setter
public class ThrowingDestination {

    public ThrowingDestination() {
        throw new IllegalStateException();
    }

}
