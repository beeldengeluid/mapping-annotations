package nl.beeldengeluid.mapping.impl;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

import nl.beeldengeluid.mapping.*;


/**
 * Sometimes the object found at a leaf is a {@code String} but needs to be {@code long}, or the other way around.
 * These kind of straight-forward mappings are collected here.
 */
@Getter
@EqualsAndHashCode
public class ScalarMapper implements LeafMapper {

    public static final ScalarMapper INSTANCE = new ScalarMapper();



    private ScalarMapper() {
    }

    @Override
    public Leaf map(Mapper mapper, MappedField destinationField, Object o) {
        if (CharSequence.class.isAssignableFrom(destinationField.type()) && ! (o instanceof CharSequence)) {
            return LeafMapper.mapped(o.toString());
        } else if (Long.class.isAssignableFrom(destinationField.type())) {
            if (o instanceof CharSequence string) {
                return LeafMapper.mapped(Long.parseLong(o.toString()));
            }
        } else if (Integer.class.isAssignableFrom(destinationField.type())) {
            if (o instanceof CharSequence string) {
                return LeafMapper.mapped(Integer.parseInt(o.toString()));
            }
        } else if (Boolean.class.isAssignableFrom(destinationField.type())) {
            if (o instanceof CharSequence string) {
                return LeafMapper.mapped(Boolean.parseBoolean(o.toString()));
            }
        } else if (Instant.class.isAssignableFrom(destinationField.type())) {
            if (o instanceof CharSequence string) {
                return LeafMapper.mapped(Instant.parse(o.toString()));
            }
        } else if (LocalDate.class.isAssignableFrom(destinationField.type())) {
            if (o instanceof CharSequence string) {
                return LeafMapper.mapped(LocalDate.parse(o.toString()));
            }
        }
        return LeafMapper.NOT_MAPPED;
    }
}
