package nl.beeldengeluid.mapping.destinations;

import io.github.threetenjaxb.core.LocalDateXmlAdapter;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.databind.JsonNode;

import nl.beeldengeluid.mapping.annotations.Source;

import nl.beeldengeluid.mapping.sources.ExtendedSourceObject;
import nl.beeldengeluid.mapping.sources.SourceObject;


/**
 * A generic destination that mixes all kind of things.
 */
@Getter
@Setter
public class Destination {

    public Destination() {

    }

    @Source(field = "anotherJson", jsonPointer ="/title") // doesn't exist in SourceObject
    @Source(field = "json", jsonPointer ="/title", sourceClass = SourceObject.class)
    String title;


    @Source(field = "moreJson", jsonPointer ="/a/b/value")
    String description;

    @Source(jsonPointer = "/")
    JsonNode moreJson;

    @Source(field = "subObject", path="id", sourceClass = ExtendedSourceObject.class)
    Long id;

    @Source(field = "moreJson", jsonPointer ="/nisv.currentbroadcaster")
    List<SubDestinationObject> list;

    @Source(field = "moreJson", jsonPath ="['nisv.currentbroadcaster'][*]['currentbroadcaster.broadcaster']")
    List<SubDestinationObject> list2;

    @Source(field = "json", jsonPointer = "/sub")
    SubDestination sub;

    @Source(field = "json", jsonPath = "subs")
    List<SubDestination> subs;

    @Source(field ="json", jsonPath = "enum")
    ExampleEnum enumValue;


    @XmlJavaTypeAdapter(LocalDateXmlAdapter.class)
    @Source(field ="moreJson", jsonPath = "date")
    LocalDate localDate;

    @Source(field = "durationInMillis")
    Duration duration;

    @Source(field ="moreJson", jsonPath = "subObject")
    SubObject subObject;

}
