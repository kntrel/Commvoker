package com.kntrel.mc.commvoker.argument;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@FunctionalInterface
public interface ParameterPredicate<T> {

    boolean test(Class<T> clazz, Parameter parameter, Method declaringMethod);

}
