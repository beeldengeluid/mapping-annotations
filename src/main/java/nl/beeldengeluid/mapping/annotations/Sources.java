/*
 * Copyright (C) 2024 Licensed under the Apache License, Version 2.0
 */
package nl.beeldengeluid.mapping.annotations;

import java.lang.annotation.*;

/**
 * Just the wrapper annotation for {@link Source}. Can be implicit nowadays.
 * Just add multiple {@link Source}s.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE_PARAMETER})
public @interface Sources {

    /**
     * The {@link Source} annotations that this is wrapping
     */
    Source[] value();
}
