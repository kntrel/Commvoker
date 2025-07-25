package com.kntrel.mc.commvoker.argument.bind;

import com.kntrel.mc.commvoker.argument.ArgumentContext;
import com.kntrel.util.Priority;
import java.lang.annotation.Annotation;
import java.util.function.Predicate;

public interface SimpleArgumentBinding<T> extends Predicate<ArgumentContext>, Comparable<SimpleArgumentBinding<T>> {
    Class<T> toClass();
    Class<? extends Annotation> toAnnotation();
    Predicate<ArgumentContext> toCondition();
    Priority priority();

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

    default @Override int compareTo(SimpleArgumentBinding<T> o) {
        return this.priority().compareTo(o.priority());
    }
}
