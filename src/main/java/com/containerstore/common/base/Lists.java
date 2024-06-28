package com.containerstore.common.base;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.MoreObjects.firstNonNull;

public final class Lists {

    private Lists() {
        throw new UnsupportedOperationException();
    }

    public static <T> List<T> nullToEmpty(List<T> input) {
        return firstNonNull(input, new ArrayList<T>());
    }
}
