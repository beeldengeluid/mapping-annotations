package nl.beeldengeluid.mapping.impl;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import nl.beeldengeluid.mapping.Destination;
import nl.beeldengeluid.mapping.ExtendedSourceObject;
import nl.beeldengeluid.mapping.SourceObject;
import nl.beeldengeluid.mapping.SubSourceObject;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.*;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static nl.beeldengeluid.mapping.Mapper.MAPPER;

@Log4j2
class JsonUtilTest {

    static {
        Configuration.setDefaults(new Configuration.Defaults() {

            private final JsonProvider jsonProvider = new JacksonJsonNodeJsonProvider();
            private final MappingProvider mappingProvider = new JacksonMappingProvider();

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }
        });
    }

    @Test
    public void unwrap() throws JsonProcessingException {
        JsonNode node = JsonUtil.MAPPER.readTree("""
           [
           null,
           true,
           1,
           1.0,
           [1, 2, 3],
           "text"
           ]
           """);
       List<Object> unwrapped = (List<Object>) JsonUtil.unwrapJson(node);
       assertThat(unwrapped).containsExactly(
           null,
           Boolean.TRUE,
           1,
           1.0,
           List.of(1, 2, 3),
           "text"
       );
    }

    @Test
    void getValue() throws NoSuchFieldException {
        ExtendedSourceObject sourceObject = new ExtendedSourceObject();
        sourceObject.json("{'title': 'foobar'}".getBytes(StandardCharsets.UTF_8));


        Optional<Object> title = JsonUtil.getSourceValueFromJson(sourceObject, Destination.class, Destination.class.getDeclaredField("title"), List.of());
        assertThat(title).contains("foobar");
    }


    @Test
    public void mapJsonObject() throws JsonProcessingException {
        SubSourceObject subObject = new SubSourceObject();

        JsonNode node = new ObjectMapper().readTree("""
          {
               "currentbroadcaster.broadcaster": {
                            "value": "209345",
                              "origin": "https://lab-vapp-bng-01.mam.beeldengeluid.nl/api/metadata/thesaurus/~THE30/209345",
                              "resolved_value": "VPRO"
                            }
          }
          """);
        MAPPER.map(node, subObject);

        assertThat(subObject.broadcaster()).isEqualTo("VPRO");

    }

    @Test
    void list() throws NoSuchFieldException {
        SourceObject source = new SourceObject();
        source.moreJson("""
          {
            "nisv.currentbroadcaster": [
                          {
                            "currentbroadcaster.broadcaster": {
                              "value": "209345",
                              "origin": "https://lab-vapp-bng-01.mam.beeldengeluid.nl/api/metadata/thesaurus/~THE30/209345",
                              "resolved_value": "VPRO"
                            }
                          },
                          {
                            "currentbroadcaster.broadcaster": {
                              "value": "209346",
                              "origin": "https://lab-vapp-bng-01.mam.beeldengeluid.nl/api/metadata/thesaurus/~THE30/209346",
                              "resolved_value": "TROS"
                            }
                          }
                        ]
          }
          """);


        List<SubSourceObject> list = (List<SubSourceObject>) JsonUtil.getSourceValueFromJson(source, Destination.class, Destination.class.getDeclaredField("list"), List.of()).orElseThrow();

        assertThat(list).hasSize(2);

        assertThat(list.get(0).broadcaster()).isEqualTo("VPRO");

    }

    @Test
    void list2() throws NoSuchFieldException, IOException {
        SourceObject source = new SourceObject();
        source.moreJson("""
          {
            "nisv.currentbroadcaster": [
                          {
                            "currentbroadcaster.broadcaster": {
                              "value": "209345",
                              "origin": "https://lab-vapp-bng-01.mam.beeldengeluid.nl/api/metadata/thesaurus/~THE30/209345",
                              "resolved_value": "VPRO"
                            }
                          },
                          {
                            "currentbroadcaster.broadcaster": {
                              "value": "209346",
                              "origin": "https://lab-vapp-bng-01.mam.beeldengeluid.nl/api/metadata/thesaurus/~THE30/209346",
                              "resolved_value": "TROS"
                            }
                          }
                        ]
          }
          """);
           log.info("Hoi");
           JsonNode node = new ObjectMapper().readTree(source.moreJson());



        MappingProvider mappingProvider = new JacksonMappingProvider();

        List<SubSourceObject> list2 = (List<SubSourceObject>) JsonUtil.getSourceValueFromJson(source, Destination.class, Destination.class.getDeclaredField("list2"), List.of()).orElseThrow();

        assertThat(list2).hasSize(2);

    }

    @Test

    public void jsonPath() {
        Object read = JsonPath.read("""
            {
                      "subs": []
            }
            """, "subs");
        log.info("{}", read);
    }




}
