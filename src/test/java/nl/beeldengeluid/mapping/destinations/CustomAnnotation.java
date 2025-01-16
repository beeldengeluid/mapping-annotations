package nl.beeldengeluid.mapping.destinations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import nl.beeldengeluid.mapping.annotations.Source;

@Source(leafMappers = CustomLeafMapper.class, field = "json", jsonPointer = "")
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomAnnotation {
}
