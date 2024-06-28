package com.containerstore.common.base;

import java.util.Optional;

public final class Optionals {

    private Optionals() {
        throw new UnsupportedOperationException();
    }

    public static <T> Optional<T> firstPresent(Optional<T> ... optionals) {
        for (Optional<T> optional : optionals) {
            if (optional.isPresent()) {
                return optional;
            }
        }
        return Optional.empty();
    }
}
