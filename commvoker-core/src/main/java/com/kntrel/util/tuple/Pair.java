package com.kntrel.util.tuple;

import com.kntrel.util.tuple.impl.SimplePair;

public interface Pair<A, B> {

    static <A, B> Pair<A, B> of(A first, B second) {
        return new SimplePair<>(first, second);
    }

    A first();
    B second();
}
