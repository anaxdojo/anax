package org.anax.framework.model;

import java.util.function.Supplier;
import java.util.stream.Stream;

public interface DataSupplier<T>  {
    public Stream<Supplier<T>> supplyResults();
}
