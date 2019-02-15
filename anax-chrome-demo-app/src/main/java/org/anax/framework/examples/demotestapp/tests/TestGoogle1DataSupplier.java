package org.anax.framework.examples.demotestapp.tests;

import org.anax.framework.model.DataSupplier;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;
import java.util.stream.Stream;

@Service
public class TestGoogle1DataSupplier implements DataSupplier {
    @Override
    public Stream<Supplier> supplyResults() {
        return Stream.of(
                () -> {

                    return "Hello";
                },
                () -> {
                    return "World";
                }
        );
    }
}
