package nl.beeldengeluid.mapping.impl;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

import nl.beeldengeluid.mapping.*;

import static nl.beeldengeluid.mapping.LeafMapper.mapped;


/**
 * Sometimes the object found at a leaf is a {@code String} but needs to be {@code long}, or the other way around.
 * These kind of straight-forward mappings are collected here.
 */
@Getter
@EqualsAndHashCode
public class ScalarLeafMapper implements LeafMapper {

    public static final ScalarLeafMapper INSTANCE = new ScalarLeafMapper();



    private ScalarLeafMapper() {
    }

    @Override
    public Leaf map(Mapper mapper, EffectiveSource effectiveSource,  MappedField destinationField, Object o) {
        Class<?> type = destinationField.type();
        if (type.isInstance(o)) {
            return LeafMapper.NOT_MAPPED;
        }
        if (type.isAssignableFrom(String.class)) {
            return mapped(o.toString());
        } else if (type.isAssignableFrom(Long.class)) {
            if (o instanceof CharSequence string) {
                return mapped(Long.parseLong(o.toString()));
            }
        } else if (type.isAssignableFrom(Integer.class)) {
            if (o instanceof CharSequence string) {
                return mapped(Integer.parseInt(o.toString()));
            }
        } else if (type.isAssignableFrom(Boolean.class)) {
            if (o instanceof CharSequence string) {
                return mapped(Boolean.parseBoolean(o.toString()));
            }
        } else if (type.isAssignableFrom(Instant.class)) {
            if (o instanceof CharSequence string) {
                return mapped(Instant.parse(o.toString()));
            }
        } else if (type.isAssignableFrom(LocalDate.class)) {
            if (o instanceof CharSequence string) {
                return mapped(LocalDate.parse(o.toString()));
            }
        }
        return LeafMapper.NOT_MAPPED;
    }
}
