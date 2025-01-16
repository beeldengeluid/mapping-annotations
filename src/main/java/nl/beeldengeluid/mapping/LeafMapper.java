package nl.beeldengeluid.mapping;

/**
 * At the end of the mapping process, the actual found value can be mapped. All kind of strategies can be though of. That's why it is pluggable.
 */
@FunctionalInterface
public interface LeafMapper extends Comparable<LeafMapper> {

    /**
     * Try to map a leaf object
     * @param mapper The mapper for which this is happening. It can e.g. be used for 'sub' mappings.
     * @param destinationField Information about the field the mapped value has to be stored in
     * @param o The incoming object
     * @return A {@link Leaf} object describing the result of the mapping process.
     */
    Leaf map(Mapper mapper, EffectiveSource effectiveSource,  MappedField destinationField, Object o);


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

    static Leaf mappedTerminal(Object o) {
        return new Leaf(o, true, true);
    }

    static Leaf mapped(Object o) {
        return new Leaf(o, true, false);
    }

    /**
     * The result of a {@link #map(Mapper, EffectiveSource, MappedField, Object)}
     * An object combined with a boolean 'success', indication whether the
     * {@link LeafMapper} indeed succeeded mapping the source value to something
     * better.
     * @param result The resulting object ({@code null} if {@code ! success()}
     * @param success Whether leaf-mapping did anything
     * @param terminate If false, this indicates that further {@link LeafMapper}s need not be consulted
     */
    record Leaf(Object result, boolean success, boolean terminate) {};
}
