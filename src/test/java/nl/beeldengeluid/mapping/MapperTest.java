package nl.beeldengeluid.mapping;

import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import nl.beeldengeluid.mapping.destinations.*;
import nl.beeldengeluid.mapping.sources.*;

import static nl.beeldengeluid.mapping.Mapper.MAPPER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Log4j2
class MapperTest {

    @Test
    public void test() {
        Destination destination = new Destination();
        SourceObject sourceObject = new SourceObject();
        sourceObject.json("{'title': 'foobar'}".getBytes(StandardCharsets.UTF_8));

        MAPPER.map(sourceObject, destination);;
        log.info("{}", destination);
        assertThat(destination.title()).isEqualTo("foobar");
        assertThat(destination.moreJson()).isEqualTo(sourceObject.moreJson());
    }

    @Test
    public void test2() {
        Destination destination = new Destination();
        AnotherSource sourceObject = AnotherSource.of("""
        {"title": "foobar"}
        """
        );

        MAPPER.map(sourceObject, destination);;
        log.info("{}", destination);
        assertThat(destination.title()).isEqualTo("foobar");
    }

    @Test
    public void time() {
        String moreJson = """
            {"title": "foobar"}
            """;
        Mapper mapper = MAPPER.withClearsJsonCacheEveryTime(false).withSupportsJaxbAnnotations(false);
        Instant start = Instant.now();
        for (int i = 0; i < 1_000; i++) {
            Destination destination = new Destination();
            ExtendedSourceObject sourceObject = new ExtendedSourceObject();
            sourceObject.title("foobar");
            //sourceObject.subObject(new SubSourceObject("a", null, 1L ));
            sourceObject.moreJson(moreJson);

            mapper.map(sourceObject, destination);

            log.debug("{}", destination);
        }
        log.info("Took {}", Duration.between(start, Instant.now()));
    }

    @Test
    public void toRecord() {
        SourceObject sourceObject = new SourceObject().title("bla bla");
        var builder = DestinationRecord.builder();
        MAPPER.map(sourceObject, builder);
        var r = builder.build();
        assertThat(r.title()).isEqualTo("bla bla");
    }



   @Test
   void getMappedDestinationProperties() {
       assertThat(MAPPER.getMappedDestinationProperties(
           ExtendedSourceObject.class,
           Destination.class
       ).keySet()).containsExactlyInAnyOrder("title", "description", "moreJson", "id", "list", "list2", "sub", "subs", "enumValue", "localDate", "duration", "subObject");

       assertThat(MAPPER.getMappedDestinationProperties(
           SourceObject.class,
           Destination.class
       ).keySet()).containsExactlyInAnyOrder("title", "description", "moreJson", "list", "list2", "sub", "subs", "enumValue", "localDate", "duration", "subObject");
   }

    @Test
    void getMappedDestinationProperties2() {
        assertThat(MAPPER.getMappedDestinationProperties(AnotherSource.class , Destination.class).keySet()).containsExactlyInAnyOrder("title");
    }


    @Test
    void withDefaults() {
        SourceObject sourceObject = new SourceObject();
        sourceObject.json("""
            {
                title: "foo",
                description: "bar"
            }
            """.getBytes(StandardCharsets.UTF_8));

        FromJsonFieldDestination anotherDestination = MAPPER.map(sourceObject, FromJsonFieldDestination.class);
        assertThat(anotherDestination.title()).isEqualTo("foo");
        assertThat(anotherDestination.description()).isEqualTo("bar");

    }

    @Test
    void mapException() {
        assertThatThrownBy(() -> {
            MAPPER.map(new Object(), ThrowingDestination.class);
        }).isInstanceOf(MapException.class);
    }

    @Test
    void customMapping() {


        Mapper mapper = MAPPER.withLeafMapper((m,  s, field, value) -> {
                if (value instanceof JsonNode json && field.genericType().equals(SubDestination.class)) {
                    Field f;
                    SubDestination so = new SubDestination();
                    so.a(json.get("title").asText() + "/" + json.get("description").asText());
                    return LeafMapper.mapped(so);
                } else {
                    return LeafMapper.NOT_MAPPED;
                }
            }
        );

        SourceObject sourceObject = new SourceObject();
        sourceObject.json("""
            { sub: {
                title: "foo",
                description: "bar"
                }
            }
            """.getBytes(StandardCharsets.UTF_8));

        Destination destination = mapper.map(sourceObject, Destination.class);
        assertThat(destination.sub().a()).isEqualTo("foo/bar");
    }


    @Test
    void customMappingForList() {

        Mapper mapper = MAPPER.withLeafMapper((m, s, field, value) -> {
            if (value instanceof JsonNode json && field.genericType().equals(SubDestination.class)) {
                if (json.isObject() && json.has("title") && json.has("description")) {
                    SubDestination so = new SubDestination();
                    so.a(json.get("title").asText() + "/" + json.get("description").asText());
                    return LeafMapper.mapped(so);
                }
            }
            return LeafMapper.NOT_MAPPED;

        }).withLeafMapper((m, effectiveSource, field, value) -> {
            if (value instanceof SubDestination s) {
                if (s.b() == null) {
                    s.b(field.name());
                }
            }
            return LeafMapper.mapped(value);
        });

        SourceObject sourceObject = new SourceObject();
        sourceObject.json("""
            { "subs" : [
            {
                title: "foo",
                description: "bar"
            }]}
            """.getBytes(StandardCharsets.UTF_8));

        Destination destination = mapper.map(sourceObject, Destination.class);
        assertThat(destination.subs().get(0).a()).isEqualTo("foo/bar");
        assertThat(destination.subs().get(0).b()).isEqualTo("subs");

    }


    @Test
    void enums() {
        Mapper mapper = MAPPER.withClearsJsonCacheEveryTime(false);
        SourceObject sourceObject = new SourceObject();
        {
            sourceObject.json("""
                { "enum" : "a" }
                """.getBytes(StandardCharsets.UTF_8));

            Destination destination = mapper.map(sourceObject, Destination.class);
            assertThat(destination.enumValue()).isEqualTo(ExampleEnum.a);
        }
    }

    @Test
    void xmlenums() {
        Mapper mapper = MAPPER;
        SourceObject sourceObject = new SourceObject();
        {
            sourceObject.json("""
                { "enum" : "alfa" }
                """.getBytes(StandardCharsets.UTF_8));

            Destination destination = mapper.map(sourceObject, Destination.class);
            assertThat(destination.enumValue()).isEqualTo(ExampleEnum.a);

            Destination destination2 = mapper.withSupportsJaxbAnnotations(false).map(sourceObject, Destination.class);
            assertThat(destination2.enumValue()).isNull();
        }
    }


    @Test
    void xmlAdapter() {
        SourceObject sourceObject = new SourceObject();

        sourceObject.moreJson("""
            { "date" : "2024-12-09" }
            """);

        Destination destination = MAPPER.map(sourceObject, Destination.class);
        assertThat(destination.localDate()).isEqualTo("2024-12-09");

    }

    @Test
    void customMappingDuration() {


        Mapper mapper = MAPPER.withLeafMapper((m, s, field, value) -> {
                if (field.genericType().equals(Duration.class)) {
                    if (value instanceof Number number) {
                        return LeafMapper.mapped(Duration.ofMillis(number.longValue()));
                    }
                }
                return LeafMapper.NOT_MAPPED;
            }
        );

        SourceObject sourceObject = new SourceObject();
        sourceObject.durationInMillis(1000L);
        Destination destination = mapper.map(sourceObject, Destination.class);
        assertThat(destination.duration()).isEqualTo(Duration.ofMillis(1000));
    }

    @Test
    public void subJson() {
        SourceObject source = new SourceObject();
        source.moreJson("""
          {
            "otherField": {},
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
        Destination destination = MAPPER.map(source, Destination.class);

        assertThat(destination.list().get(0).broadcaster()).isEqualTo("VPRO");
        //assertThat(destination.subObject().b()).isEqualTo("bar");

    }

    @Test
    public void multipleSourcesA() {
        SourceObject source = new SourceObject();
        source.moreJson("""
            {
              "a": "x"
            }
            """);
        {
            MultipleSources destination = MAPPER.map(source, MultipleSources.class);
            assertThat(destination.a).isEqualTo("x");
        }
    }
    @Test
    public void multipleSourcesB() {
        SourceObject source = new SourceObject();
        source.moreJson("""
          {
            "b": "y"
          }
          """);
        {
            MultipleSources destination = MAPPER.map(source, MultipleSources.class);
            assertThat(destination.a).isEqualTo("y");
        }
        source.moreJson("""
          {
            "a": "x",
            "b": "y"
          }
          """);
        {
            MultipleSources destination = MAPPER.map(source, MultipleSources.class);
            assertThat(destination.a).isEqualTo("y");
        }

    }


    @Test
    public void multipleSourcesWithLeafMapper() {
        Mapper mapper = MAPPER.withLeafMapper(String.class, String.class, (effectiveSource, string) -> {
            if ("x".equals(string)) {
                return Optional.empty();
            }
            return Optional.ofNullable(string);

        });
        SourceObject source = new SourceObject();
        source.moreJson("""
            {
              "a": "x",
              "b": "y"
            }
            """);
        {
            MultipleSources destination = mapper.map(source, MultipleSources.class);
            assertThat(destination.a).isEqualTo("y");
        }
    }



}
