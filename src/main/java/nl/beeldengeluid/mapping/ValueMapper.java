package nl.beeldengeluid.mapping;

import java.lang.reflect.Field;
import java.util.Optional;


/**
 * At the end of the mapping process, the actual found value can be mapped. All kind of strategies can be though of. That's why it is pluggable.
 */
@FunctionalInterface
public interface ValueMapper<T> {

    /**
     * Th
     * @param destinationClass The class the converted value will be stored as a field value in
     * @param destinationField The field the converted value will be stored in
     * @param o The incoming value to convert
     * @return
     */
    ValueMap mapValue(Mapper mapper, MappedField destinationField, T o);



    ValueMap NOT_MAPPED = new ValueMap(null, false, false);

    static ValueMap mapped(Object o) {
        return new ValueMap(o, true, false);
    }

    record ValueMap(Object result, boolean success, boolean terminate) {};
}
