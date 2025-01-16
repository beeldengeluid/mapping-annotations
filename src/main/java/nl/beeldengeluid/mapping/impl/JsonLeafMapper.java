package nl.beeldengeluid.mapping.impl;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import nl.beeldengeluid.mapping.EffectiveSource;
import nl.beeldengeluid.mapping.LeafMapper;
import nl.beeldengeluid.mapping.MappedField;
import nl.beeldengeluid.mapping.Mapper;

import java.time.Instant;
import java.time.LocalDate;

import static nl.beeldengeluid.mapping.LeafMapper.mapped;


/**
 * Sometimes the object found at a leaf is a {@code String} but needs to be {@code long}, or the other way around.
 * These kind of straight-forward mappings are collected here.
 */
@Getter
@EqualsAndHashCode
public class JsonLeafMapper implements LeafMapper {

    public static final JsonLeafMapper INSTANCE = new JsonLeafMapper();

    private JsonLeafMapper() {
    }

    @Override
    public Leaf map(Mapper mapper, EffectiveSource effectiveSource,  MappedField destinationField, Object o) {
        try {
            return LeafMapper.mapped(JsonUtil.getJson(o));
        } catch (Exception e) {
            return NOT_MAPPED;
        }
    }
}
