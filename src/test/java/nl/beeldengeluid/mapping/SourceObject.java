package nl.beeldengeluid.mapping;

@lombok.Data
public class SourceObject {
    byte[] json;

    String moreJson;

    String title;

    long durationInMillis;

}
