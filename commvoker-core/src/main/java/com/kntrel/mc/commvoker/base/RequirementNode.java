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
    private final List<Predicate<S>> orTests_;
    private final List<Predicate<S>> andTests_;
    private boolean orAlways_;


    //CONSTRUCTORS
    public RequirementNode() {
        this.orTests_ = new ArrayList<>();
        this.andTests_ = new ArrayList<>();
        this.orAlways_ = false;
    }


    //IMPLEMENTATION
    @Override
    public boolean test(S s) {
        return this.testOrs(s) && this.testAnds(s);
    }

    @Override @SuppressWarnings("unchecked")
    public RequirementNode<S> or(Predicate<? super S> other) {
        if (this.orAlways_) { return this; }

        if (other instanceof RequirementNode<?> node) {
            if (node.orAlways_) {
                return this.orAlways();
            } else {
                node.orTests_.forEach(t -> this.orTests_.add((Predicate<S>) t));
            }
        } else {
            this.orTests_.add((Predicate<S>) other);
        }
        return this;
    }

    @Override @SuppressWarnings("unchecked")
    public RequirementNode<S> and(Predicate<? super S> other) {
        if (other instanceof RequirementNode<?> node) {
            node.andTests_.forEach(t -> this.andTests_.add((Predicate<S>) t));
        } else {
            this.andTests_.add((Predicate<S>) other);
        }
        return this;
    }

    public RequirementNode<S> orAlways() {
        this.orAlways_ = true;
        this.orTests_.clear();
        return this;
    }


    //HELPERS
    private boolean testOrs(S s) {
        if (this.orAlways_) { return true; }
        if (this.orTests_.isEmpty()) { return true; }
        for (Predicate<S> test : this.orTests_) {
            if (test == null) { continue; }
            if (test.test(s)) { return true; }
        }
        return false;
    }
    private boolean testAnds(S s) {
        for (Predicate<S> test : this.andTests_) {
            if (test == null) { continue; }
            if (!test.test(s)) { return false; }
        }
        return true;
    }
}
