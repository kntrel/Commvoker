package com.kntrel.util.tuple;
import java.util.Objects;

class SimplePair<A, B> implements Pair<A, B> {

    private final A first_;
    private final B second_;

    public SimplePair(A a, B b) {
        this.first_ = a;
        this.second_ = b;
    }

    @Override public A first() {
        return this.first_;
    }

    @Override public B second() {
        return this.second_;
    }

    @Override public boolean equals(Object o) {
        if (o == null) { return false; }
        if (o == this) { return true; }
        if (!(o instanceof Pair<?,?> other)) { return false; }
        return this.first_.equals(other.first()) && this.second_.equals(other.second());
    }

    @Override public int hashCode() {
        return Objects.hash(this.first_, this.second_);
    }

}
