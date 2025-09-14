package com.kntrel.mc.commvoker.base;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

class RequirementNode<S> implements Predicate<S> {

    //FACTORY
    private static final RequirementNode<?> ALWAYS = new RequirementNode<>().orAlways();
    public static <S> RequirementNode<S> always() {
        return (RequirementNode<S>) ALWAYS;
    }


    //FIELDS
    private final List<Predicate<S>> tests_;
    private boolean passThrough_;


    //CONSTRUCTORS
    public RequirementNode() {
        this.tests_ = new ArrayList<>();
        this.passThrough_ = false;
    }


    //IMPLEMENTATION
    @Override
    public boolean test(S s) {
        if (this.passThrough_) { return true; }
        if (this.tests_.isEmpty()) { return true; }
        for (Predicate<S> test : this.tests_) {
            if (test == null) { continue; }
            if (test.test(s)) { return true; }
        }
        return false;
    }

    @Override @SuppressWarnings("unchecked")
    public RequirementNode<S> or(Predicate<? super S> other) {
        if (this.passThrough_) { return this; }

        if (other instanceof RequirementNode<?> node) {
            if (node.passThrough_) {
                return this.orAlways();
            } else {
                node.tests_.forEach(t -> this.tests_.add((Predicate<S>) t));
            }
        } else {
            this.tests_.add((Predicate<S>) other);
        }
        return this;
    }

    public RequirementNode<S> orAlways() {
        this.passThrough_ = true;
        this.tests_.clear();
        return this;
    }
}
