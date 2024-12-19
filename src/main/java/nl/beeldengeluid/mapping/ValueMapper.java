package nl.beeldengeluid.mapping;

import java.lang.reflect.Field;
import java.util.Optional;


/**
 * At the end of the mapping process, the actual found value can be mapped. All kind of strategies can be though of. That's why it is pluggable.
 */
@FunctionalInterface
public interface ValueMapper {

    ValueMap mapValue(Class<?> destinationClass, Field destinationField, Object o);


    ValueMap NOT_MAPPED = new ValueMap(null, false);

    static ValueMap mapped(Object o) {
        return new ValueMap(o, true);
    }

    record ValueMap(Object result, boolean success) {};
}
