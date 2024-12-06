package org.meeuw.mapping;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.log4j.Log4j2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import static org.meeuw.mapping.Mapper.MAPPER;

@Log4j2
class MapperTest {

    @Test
    public void test() {
        Destination destination = new Destination();
        SourceObject sourceObject = new SourceObject();
        sourceObject.setJson("{'title': 'foobar'}".getBytes(StandardCharsets.UTF_8));

        MAPPER.map(sourceObject, destination);;
        log.info("{}", destination);
        assertThat(destination.getTitle()).isEqualTo("foobar");
        assertThat(destination.getMoreJson()).isEqualTo(sourceObject.getMoreJson());

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
        assertThat(destination.getTitle()).isEqualTo("foobar");
    }

    @Test
    public void time() {
        String moreJson = """
            {"title": "foobar"}
            """;
        Mapper mapper = MAPPER.withClearJsonCache(false);
        Instant start = Instant.now();
        for (int i = 0; i < 1_000_000; i++) {
            Destination destination = new Destination();
            ExtendedSourceObject sourceObject = new ExtendedSourceObject();
            sourceObject.setTitle("foobar");
            sourceObject.setSubObject(new SubObject("a", null, 1L ));
            sourceObject.setMoreJson(moreJson);

            mapper.map(sourceObject, destination);

            log.debug("{}", destination);
        }
        log.info("Took {}", Duration.between(start, Instant.now()));
    }

    @Test
    public void toRecord() {
        SourceObject sourceObject = new SourceObject();
        sourceObject.setTitle("bla bla");
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
       ).keySet()).containsExactlyInAnyOrder("title", "description", "moreJson", "id", "list", "list2");

       assertThat(MAPPER.getMappedDestinationProperties(
           SourceObject.class,
           Destination.class
       ).keySet()).containsExactlyInAnyOrder("title", "description", "moreJson", "list", "list2");
   }

    @Test
    void getMappedDestinationProperties2() {
        assertThat(MAPPER.getMappedDestinationProperties(AnotherSource.class , Destination.class).keySet()).containsExactlyInAnyOrder("title");
    }


    @Test
    void withDefaults() {
        SourceObject sourceObject = new SourceObject();
        sourceObject.setJson("""
            {
                title: "foo",
                description: "bar"
            }
            """.getBytes(StandardCharsets.UTF_8));

        AnotherDestination anotherDestination = MAPPER.map(sourceObject, AnotherDestination.class);
        assertThat(anotherDestination.getTitle()).isEqualTo("foo");
        assertThat(anotherDestination.getDescription()).isEqualTo("bar");

    }

    @Test
    void mapException() {
        assertThatThrownBy(() -> {
            MAPPER.map(new Object(), ThrowingDestination.class);
        }).isInstanceOf(MapException.class);
    }


}