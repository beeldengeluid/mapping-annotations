package nl.beeldengeluid.mapping;

import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class ThrowingDestination {

    public ThrowingDestination() {
        throw new IllegalStateException();
    }

}
