package nl.beeldengeluid.mapping.impl;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import lombok.extern.slf4j.Slf4j;

import nl.beeldengeluid.mapping.ValueMapper;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class JaxbValueMapper implements ValueMapper {

    private static final Map<Field, Optional<XmlAdapter<?, ?>>> ADAPTERS = new ConcurrentHashMap<>();

    private JaxbValueMapper() {}

    public static final JaxbValueMapper INSTANCE = new JaxbValueMapper();


    private static ValueMap considerXmlAdapter(Object o, Field destinationField)  {
        Optional<XmlAdapter<?, ?>> adapter = ADAPTERS.computeIfAbsent(destinationField, (field) -> {
            XmlJavaTypeAdapter annotation = field.getAnnotation(XmlJavaTypeAdapter.class);
            if (annotation != null) {
                try {
                    XmlAdapter<?, ?> xmlAdapter = annotation.value().getDeclaredConstructor().newInstance();
                    return Optional.of(xmlAdapter);
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }
            }
            return Optional.empty();
        });
        if (adapter.isPresent()) {
            try {
                //noinspection unchecked,rawtypes
                return ValueMapper.mapped(((XmlAdapter) adapter.get()).unmarshal(o));
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
        } else {

        }
        return NOT_MAPPED;
    }

    @Override
    public ValueMap mapValue(Class<?> destinationClass, Field destinationField, Object o) {
        return considerXmlAdapter(o, destinationField);
    }
}
