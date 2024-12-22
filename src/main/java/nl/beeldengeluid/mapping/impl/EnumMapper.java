package nl.beeldengeluid.mapping.impl;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.lang.reflect.Field;

import jakarta.xml.bind.annotation.XmlEnumValue;

import nl.beeldengeluid.mapping.LeafMapper;
import nl.beeldengeluid.mapping.*;

@Getter
@EqualsAndHashCode
public class EnumMapper implements LeafMapper {


    private final boolean considerXmlEnum;

    public EnumMapper(boolean considerXmlEnum) {
        this.considerXmlEnum = considerXmlEnum;
    }

    @Override
    public Leaf map(Mapper mapper, MappedField destinationField, Object o) {

        if (destinationField.genericType() instanceof  Class<?> c && c.isEnum() && o instanceof String string) {
            Class<Enum<?>> enumClass = (Class<Enum<?>>) c;
            if (considerXmlEnum) {
                for (Enum<?> enumConstant : enumClass.getEnumConstants()) {
                    try {
                        Field f = enumConstant.getDeclaringClass().getField(enumConstant.name());
                        XmlEnumValue xmlValue = f.getAnnotation(XmlEnumValue.class);
                        if (xmlValue != null && xmlValue.value().equals(string)) {
                            return LeafMapper.mapped(enumConstant);
                        }
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            for (Enum<?> enumConstant : enumClass.getEnumConstants()) {
                if (enumConstant.name().equals(string)) {
                    return LeafMapper.mapped(enumConstant);
                }
            }

        }
        return LeafMapper.NOT_MAPPED;
    }
}
