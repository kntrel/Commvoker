package com.kntrel.util.tuple.impl;

import com.kntrel.util.tuple.Triplet;
import java.util.Objects;

public class SimpleTriplet<A, B, C> extends SimplePair<A, B> implements Triplet<A, B, C> {

    private final C third_;

    public SimpleTriplet(A a, B b, C c) {
        super(a, b);
        this.third_ = c;
    }

    @Override public C third() {
        return this.third_;
    }

    @Override public boolean equals(Object o) {
        if (!super.equals(o)) { return false; }
        if (!(o instanceof Triplet<?,?,?> other)) { return false; }
        return this.third_.equals(other.third());
    }

    @Override public int hashCode() {
        return super.hashCode() + Objects.hash(this.third_);
    }
}
