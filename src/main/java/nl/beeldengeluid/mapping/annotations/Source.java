/*
 * Copyright (C) 2024 Licensed under the Apache License, Version 2.0
 */
package nl.beeldengeluid.mapping.annotations;

import java.lang.annotation.*;

import nl.beeldengeluid.mapping.LeafMapper;
import nl.beeldengeluid.mapping.Mapper;

/**
 * This annotation can be put on a field of some destination object, to indicate
 * from which other object's field it must come. It can also be used at {@code class} level, in which case
 * its values serve as <em>default</em> values for the {@code Source} annotations on its fields.
 * <p>
 * Multiple of these annotation can be used together, in which case the best one for the situation will be used.
 *
 * @author Michiel Meeuwissen
 * @since 0.1
 * @see Sources
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE_PARAMETER, ElementType.TYPE})
@Repeatable(Sources.class)
public @interface Source {

    /**
     * The default value for all {@link String} valued properties of this {@code @interface}.
     * This can be used to check whether a certain property was explicitly set or not.
     */
    String UNSET = "#UNSET";

    /**
     * The source class in with the other field can be found
     * <p>
     * Optional, normally just in the source class of the source object
     * But multiple source annotations can be present, and the one where the sourceClass
     * matches the actual class of the source object will be used then.
     * @return a class which match on the actual type of the source object
     */
    Class<?> sourceClass() default Object.class;


    /**
     * Json path inside the other field. If not specified, it may be the entire field
     * Requires jways
     * @since 0.2
     * @return a json path e.g. {@code title.value}
     */
    String jsonPath() default UNSET;

    /**
     * Json pointer inside the other field.
     * @since 0.2
     * @return a json pointer. E.g. {@code /title/value}
     */
    String jsonPointer() default UNSET;


    /**
     * Name of the field in the source class, if not specified this means either
     * 1. The source field has the same name as the name of the field in the destination
     * 2. The source has become some json object and is not any more associated with a concrete field
     * (an example to explain this would probably be welcome)
     * @return The name of the field in the source class
     */
    String field() default UNSET;


    /**
     * 'Deeper' values can be addresses via a path of more fields.
     * @return An array of field names in sub objects
     */
    String[] path() default {};

    /**
     * @see Mapper#leafMappers()
     */
    Class<? extends LeafMapper>[] leafMappers() default {};





}
