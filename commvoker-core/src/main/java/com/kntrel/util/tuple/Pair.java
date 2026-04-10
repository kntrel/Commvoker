package com.kntrel.util.tuple;

/**
 * Internal API. Public for framework wiring, but not part of the supported external API.
 */
public interface Pair<A, B> {

    static <A, B> Pair<A, B> of(A first, B second) {
        return new SimplePair<>(first, second);
    }

    A first();
    B second();
}
