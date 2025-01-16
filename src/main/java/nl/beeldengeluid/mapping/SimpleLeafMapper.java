package nl.beeldengeluid.mapping;

import java.util.Optional;


/**
 * An abstract version of {@link LeafMapper} that just reduces the needed {@link #map(Mapper, EffectiveSource, MappedField, Object)} to a simpler
 * {@link #map(EffectiveSource, Object)}
 * <p>
 * This is possible if during construction it is specified between what source and destination types this mapper can map.
 * @param <S> the source type this mapper recognizes
 * @param <D> the destination type this mapper recognizes
 */
public abstract class SimpleLeafMapper<S, D> implements LeafMapper {

    private final Class<S> source;
    private final Class<D> destination;

    protected SimpleLeafMapper(Class<S> source, Class<D> destination) {
        this.source = source;
        this.destination = destination;
    }


    @Override
    public int weight() {
        return 0;
    }
    @Override
    public final Leaf map(Mapper mapper, EffectiveSource effectiveSource, MappedField destinationField, Object o) {
        if (destination.isAssignableFrom(destinationField.type()) && source.isInstance(o)) {
            Optional<D> optionalD = map(effectiveSource, (S) o);
            if (optionalD.isPresent()) {
                return LeafMapper.mapped(optionalD.get());
            }
        }
        return LeafMapper.NOT_MAPPED;
    }

    protected abstract Optional<D> map(EffectiveSource effectiveSource, S source);
}
