package com.kntrel.mc.commvoker.argument.tuple;

import com.kntrel.mc.commvoker.argument.descriptor.ArgumentNode;
import com.kntrel.util.tuple.impl.SimplePair;
import java.util.List;

public class DualArgumentNode<S, A, B> extends SimplePair<ArgumentNode<S, A>, ArgumentNode<S, B>> {
    public DualArgumentNode(ArgumentNode<S, A> first, ArgumentNode<S, B> second) {
        super(first, second);
    }

    public List<ArgumentNode<S, ?>> toList() {
        return List.of(this.first(), this.second());
    }
}
