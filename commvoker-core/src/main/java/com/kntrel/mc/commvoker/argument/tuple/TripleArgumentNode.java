package com.kntrel.mc.commvoker.argument.tuple;

import com.kntrel.mc.commvoker.argument.descriptor.ArgumentNode;
import com.kntrel.util.tuple.Triplet;

import java.util.List;
import java.util.Objects;

public class TripleArgumentNode<S, A, B, C> extends DualArgumentNode<S, A, B> implements Triplet<ArgumentNode<S, A>, ArgumentNode<S, B>, ArgumentNode<S, C>> {

    private final ArgumentNode<S, C> third_;

    public TripleArgumentNode(ArgumentNode<S, A> first, ArgumentNode<S, B> second, ArgumentNode<S, C> third) {
        super(first, second);
        this.third_ = third;
    }

    @Override public ArgumentNode<S, C> third() {
        return this.third_;
    }

    @Override public List<ArgumentNode<S, ?>> toList() {
        return List.of(this.first(), this.second(), this.third());
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
