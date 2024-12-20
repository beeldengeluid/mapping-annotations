package nl.beeldengeluid.mapping.impl;

import jakarta.xml.bind.annotation.XmlEnumValue;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import nl.beeldengeluid.mapping.*;
import nl.beeldengeluid.mapping.ValueMapper;

import java.lang.reflect.Field;

@Getter
@EqualsAndHashCode
public class EnumValueMapper implements ValueMapper<Object> {


    private final boolean considerXmlEnum;

    public EnumValueMapper(boolean considerXmlEnum) {
        this.considerXmlEnum = considerXmlEnum;
    }

    @Override
    public ValueMap mapValue(Mapper mapper, MappedField destinationField, Object o) {

        if (destinationField.type().isEnum() && o instanceof String string) {
            Class<Enum<?>> enumClass = (Class<Enum<?>>) destinationField.type();
            if (considerXmlEnum) {
                for (Enum<?> enumConstant : enumClass.getEnumConstants()) {
                    try {
                        Field f = enumConstant.getDeclaringClass().getField(enumConstant.name());
                        XmlEnumValue xmlValue = f.getAnnotation(XmlEnumValue.class);
                        if (xmlValue != null && xmlValue.value().equals(string)) {
                            return ValueMapper.mapped(enumConstant);
                        }
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            for (Enum<?> enumConstant : enumClass.getEnumConstants()) {
                if (enumConstant.name().equals(string)) {
                    return ValueMapper.mapped(enumConstant);
                }
            }

        }
        return ValueMapper.NOT_MAPPED;
    }
}
