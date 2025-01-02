package nl.beeldengeluid.mapping;

/**
 * At the end of the mapping process, the actual found value can be mapped. All kind of strategies can be though of. That's why it is pluggable.
 */
@FunctionalInterface
public interface LeafMapper extends Comparable<LeafMapper> {

    /**
     * @param destinationField The field the converted value will be stored in
     * @param o The incoming value to convert
     * @return Leaf o
     */
    default Leaf map(Mapper mapper, MappedField destinationField, Object o, Class<?>... groups) {
        return map(mapper, destinationField, o);
    }


    Leaf map(Mapper mapper, MappedField destinationField, Object o);


    /**
     * The order of the registered 'leaf mappers' may be influenced using this.
     *
     * @return an integer. Smaller is earlier. Default to {@code 100}
     */
    default int weight() {
        return 100;
    }

    default int compareTo(LeafMapper leafMapper) {
        return weight() - leafMapper.weight();
    }


    Leaf NOT_MAPPED = new Leaf(null, false, false);

    static Leaf mapped(Object o) {
        return new Leaf(o, true, true);
    }

    /**
     * The result of a {@link #map(Mapper, MappedField, Object)}
     * An object combined with a boolean 'success', indication whether the
     * {@link LeafMapper} indeed succeeded mapping the source value to something
     * better.
     * @param result The resulting object ({@code null} if {@code ! success()}
     * @param success Whether leaf-mapping did anything
     * @param terminate If false, this indicates that further {@link LeafMapper}s need not be consulted
     */
    record Leaf(Object result, boolean success, boolean terminate) {};
}
