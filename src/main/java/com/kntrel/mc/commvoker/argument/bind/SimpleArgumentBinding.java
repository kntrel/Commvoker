package com.kntrel.mc.commvoker.argument.bind;

import com.kntrel.mc.commvoker.argument.ParameterContext;
import com.kntrel.util.Priority;
import java.lang.annotation.Annotation;
import java.util.function.Predicate;

public interface SimpleArgumentBinding<T> extends Predicate<ParameterContext> {
    Class<T> toClass();
    Class<? extends Annotation> toAnnotation();
    Predicate<ParameterContext> toCondition();
    Priority priority();

    default @Override boolean test(ParameterContext ctx) {
        Class<?> clazz = this.toClass();
        if (clazz != null && ctx.type() instanceof Class<?> c && !c.isAssignableFrom(clazz)) {
            return false;
        }

        Class<? extends Annotation> annotation = this.toAnnotation();
        if (annotation != null && !ctx.isAnnotationPresent(annotation)) {
            return false;
        }

        Predicate<ParameterContext> condition = this.toCondition();
        if (condition != null) {
            return condition.test(ctx);
        }

        return true;
    }
}
