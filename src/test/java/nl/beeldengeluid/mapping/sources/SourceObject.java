package nl.beeldengeluid.mapping.sources;

@lombok.Data
public class SourceObject {
    byte[] json;

    String moreJson;

    String title;

    Long durationInMillis;

}
