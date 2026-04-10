package com.kntrel.util.tuple;

/**
 * Internal API. Public for framework wiring, but not part of the supported external API.
 */
public interface Triplet<A, B, C> extends Pair<A, B> {

    static <A, B, C> Triplet<A, B, C> of(A first, B second, C third) {
        return new SimpleTriplet<>(first, second, third);
    }
    static <A, B, C> Triplet<A, B, C> of(Pair<A, B> pair, C third) {
        return new SimpleTriplet<>(pair.first(), pair.second(), third);
    }

    C third();
}
