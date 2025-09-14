package com.kntrel.mc.commvoker.mock;

import com.kntrel.mc.commvoker.requirement.Requires;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Requires(MockRequirement.class)
public @interface MockRequires {

    String value();
}
