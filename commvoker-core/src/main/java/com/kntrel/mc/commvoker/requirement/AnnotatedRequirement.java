package com.kntrel.mc.commvoker.requirement;

import java.lang.annotation.Annotation;

@FunctionalInterface
public interface AnnotatedRequirement<S, A extends Annotation> {

    boolean test(S source, A annotation);
}
