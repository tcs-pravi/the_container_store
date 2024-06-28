package com.containerstore.common.base;

import com.google.common.primitives.Ints;

import java.util.Collection;
import java.util.Comparator;

public final class Collections {

    private Collections() {
        throw new UnsupportedOperationException();
    }

    public static Comparator<Collection> byCollectionSizeDescending() {
        return (left, right) -> Ints.compare(right.size(), left.size());
    }
}
