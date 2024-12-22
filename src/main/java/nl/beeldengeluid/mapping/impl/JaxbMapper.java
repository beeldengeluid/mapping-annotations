package nl.beeldengeluid.mapping.impl;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import nl.beeldengeluid.mapping.LeafMapper;
import nl.beeldengeluid.mapping.*;

/**
 * Consider {@link XmlJavaTypeAdapter} annotations when mapping leaf values.
 */
@Slf4j
public class JaxbMapper implements LeafMapper {

    private static final Map<MappedField, Optional<XmlAdapter<?, ?>>> ADAPTERS = new ConcurrentHashMap<>();

    private JaxbMapper() {}

    public static final JaxbMapper INSTANCE = new JaxbMapper();


    private static Leaf considerXmlAdapter(Object o, MappedField destinationField)  {
        Optional<XmlAdapter<?, ?>> adapter = ADAPTERS.computeIfAbsent(destinationField, (field) -> {
            XmlJavaTypeAdapter annotation = field.annotation(XmlJavaTypeAdapter.class);
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
                return LeafMapper.mapped(((XmlAdapter) adapter.get()).unmarshal(o));
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
        } else {

        }
        return NOT_MAPPED;
    }

    @Override
    public Leaf map(Mapper mapper, MappedField destinationField, Object o) {
        return considerXmlAdapter(o, destinationField);
    }
}
