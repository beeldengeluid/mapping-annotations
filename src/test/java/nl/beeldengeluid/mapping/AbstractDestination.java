package nl.beeldengeluid.mapping;

import lombok.Data;

import nl.beeldengeluid.mapping.annotations.Source;

@Data
public abstract class AbstractDestination {


    @Source(jsonPointer ="/title")
    String title;
}
