package com.kntrel.mc.commvoker.requirement;


public interface Requirement<S> extends AnnotatedRequirement<S, Requires> {

    boolean test(S source);

    @Override
    default boolean test(S source, Requires annotation) { return this.test(source); }
}
