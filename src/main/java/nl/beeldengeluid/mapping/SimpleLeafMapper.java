package nl.beeldengeluid.mapping;

import java.util.function.Function;

public abstract  class SimpleLeafMapper<S, D> implements LeafMapper {


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
    public Leaf map(Mapper mapper, EffectiveSource effectiveSource,  MappedField destinationField, Object o) {
        if (destination.isAssignableFrom(destinationField.type()) && source.isInstance(o)) {
            return LeafMapper.mapped(map((S) o));
        }
        return LeafMapper.NOT_MAPPED;
    }

    protected abstract D map(S source);
}