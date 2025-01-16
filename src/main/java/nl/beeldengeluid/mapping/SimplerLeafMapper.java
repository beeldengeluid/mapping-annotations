package nl.beeldengeluid.mapping;

import java.util.Optional;


/**
 * An abstract version of {@link LeafMapper} that just reduces the needed {@link #map(Mapper, EffectiveSource, MappedField, Object)} to a simpler
 * {@link #map(Object)}
 * <p>
 * @see SimpleLeafMapper
 * @param <S> the source type this mapper recognizes
 * @param <D> the destination type this mapper recognizes
 */
public abstract class SimplerLeafMapper<S, D> extends SimpleLeafMapper<S, D> {

    protected SimplerLeafMapper(Class<S> source, Class<D> destination) {
        super(source, destination);
    }

    @Override
    protected final Optional<D> map(EffectiveSource effectiveSource, S source) {
        return Optional.ofNullable(map(source));
    }

    protected abstract D map(S source);

}
