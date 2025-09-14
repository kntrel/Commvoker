package com.kntrel.mc.commvoker.mock;

import com.kntrel.mc.commvoker.requirement.AnnotatedRequirement;
import java.util.concurrent.atomic.AtomicInteger;

public class MockRequirement implements AnnotatedRequirement<Object, MockRequires> {
    @Override
    public boolean test(Object source, MockRequires annotation) {
        if (source instanceof Person p) {
            p.setName(annotation.value());
            return true;
        }
        return false;
    }
}
