package nl.beeldengeluid.mapping.impl;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import nl.beeldengeluid.mapping.LeafMapper;
import nl.beeldengeluid.mapping.*;

/**
 * Consider {@link XmlJavaTypeAdapter} annotations when mapping leaf values. This is e.g. useful if the destination class is mappable to XML
 * and has some custom mapping at a leaf, then that can just be profited from with this.
 */
@Slf4j
public class JaxbLeafMapper implements LeafMapper {

    private static final Map<MappedField, Optional<XmlAdapter<?, ?>>> ADAPTERS = new ConcurrentHashMap<>();

    private JaxbLeafMapper() {}

    public static final JaxbLeafMapper INSTANCE = new JaxbLeafMapper();


    private static Leaf considerXmlAdapter(Object o, MappedField destinationField)  {
        if (destinationField.type().isInstance(o)) {
            // already mapped!
            return NOT_MAPPED;
        }
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
    public Leaf map(Mapper mapper, EffectiveSource effectiveSource, MappedField destinationField, Object o) {
        return considerXmlAdapter(o, destinationField);
    }
}
