package com.kntrel.mc.commvoker.requirement;

import java.util.function.Predicate;

public interface Requirement<S> extends AnnotatedRequirement<S, Requires>, Predicate<S> {

    @Override
    default boolean test(S source, Requires annotation) { return this.test(source); }
}
