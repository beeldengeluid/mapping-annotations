package nl.beeldengeluid.mapping.destinations;

import nl.beeldengeluid.mapping.annotations.Source;

@Source(field = "moreJson")
public class MultipleSources {



    @Source(jsonPointer = "/a")
    @Source(jsonPointer = "/b")
    public String a;
}
