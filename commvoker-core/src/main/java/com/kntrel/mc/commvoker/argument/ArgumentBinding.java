package com.kntrel.mc.commvoker.argument;

import com.kntrel.mc.commvoker.argument.binder.ArgumentGatherer;
import com.kntrel.mc.commvoker.argument.descriptor.ArgumentDescriptor;
import com.kntrel.util.Priority;
import java.lang.annotation.Annotation;
import java.util.function.Predicate;

public interface ArgumentBinding<S, T> extends Predicate<ArgumentContext>, Comparable<ArgumentBinding<S, T>> {

    Class<T> toClass();
    Class<? extends Annotation> toAnnotation();
    Predicate<ArgumentContext> toCondition();
    Priority priority();
    ArgumentDescriptor<S, T> descriptor(ArgumentGatherer<? extends S> ctx);

    default @Override boolean test(ArgumentContext ctx) {
        Class<?> clazz = this.toClass();
        if (clazz != null && ctx.type() instanceof Class<?> c && !c.isAssignableFrom(clazz)) {
            return false;
        }

        Class<? extends Annotation> annotation = this.toAnnotation();
        if (annotation != null && !ctx.isAnnotationPresent(annotation)) {
            return false;
        }

        Predicate<ArgumentContext> condition = this.toCondition();
        if (condition != null) {
            return condition.test(ctx);
        }

        return true;
    }
    default @Override int compareTo(ArgumentBinding<S, T> o) {
        return this.priority().compareTo(o.priority());
    }
}
