package nl.beeldengeluid.mapping.impl;

import java.lang.reflect.Field;

import jakarta.xml.bind.annotation.XmlEnumValue;

import nl.beeldengeluid.mapping.*;


/**
 * If the leaf is a String, but must be mapped to an enum, this {@link LeafMapper} can (try to) arrange it.
 * Basically it will just match on {@link Enum#name()}  (as {@link Enum#valueOf(Class, String)}), but if {@link #considerXmlEnum}
 * then it will consider the {@link XmlEnumValue} first.
 */
public record EnumLeafMapper(boolean considerXmlEnum, boolean caseSensitive) implements LeafMapper {


    @Override
    public Leaf map(Mapper mapper, EffectiveSource effectiveSource, MappedField destinationField, Object o) {

        if (destinationField.genericType() instanceof Class<?> c && c.isEnum() && o instanceof String string) {
            Class<Enum<?>> enumClass = (Class<Enum<?>>) c;
            if (considerXmlEnum) {
                for (Enum<?> enumConstant : enumClass.getEnumConstants()) {
                    try {
                        Field f = enumConstant.getDeclaringClass().getField(enumConstant.name());
                        XmlEnumValue xmlValue = f.getAnnotation(XmlEnumValue.class);
                        if (xmlValue != null && (caseSensitive ? xmlValue.value().equals(string) : xmlValue.value().equalsIgnoreCase(string))) {
                            return LeafMapper.mapped(enumConstant);
                        }
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            for (Enum<?> enumConstant : enumClass.getEnumConstants()) {
                if (caseSensitive ? enumConstant.name().equals(string) : enumConstant.name().equalsIgnoreCase(string)) {
                    return LeafMapper.mapped(enumConstant);
                }
            }

        }
        return LeafMapper.NOT_MAPPED;
    }
}
