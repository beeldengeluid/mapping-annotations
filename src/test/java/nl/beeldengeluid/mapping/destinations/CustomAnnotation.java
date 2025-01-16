package nl.beeldengeluid.mapping.destinations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import nl.beeldengeluid.mapping.annotations.Source;

/**
 * It's possible to put the @Source annotation on a custom annotation too (and annotate the field with _that_)
 */
@Source(leafMappers = CustomLeafMapper.class, field = "json", jsonPointer = "")
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomAnnotation {
}
