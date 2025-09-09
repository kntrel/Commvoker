package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.requirement.AnnotatedRequirement;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Predicate;

public class RequirementBridge<S> implements Predicate<S> {

    //FIELDS
    private final AnnotatedRequirement<?, ?> requirement_;
    private final Annotation annotation_;


    //CONSTRUCTOR
    public RequirementBridge(Class<S> sourceClass, AnnotatedRequirement<?, ?> requirement, Annotation annotation) {
        try {
            requirement.getClass().getDeclaredMethod("test", sourceClass, annotation.getClass());
        } catch (NoSuchMethodException e) {
            Method method = Arrays.stream(requirement.getClass().getDeclaredMethods())
                    .filter(m -> m.getName().equals("test"))
                    .filter(m -> {
                        Class<?>[] params = m.getParameterTypes();
                        return params.length == 2 && Annotation.class.isAssignableFrom(params[1]);
                    })
                    .findFirst()
                    .orElse(null);

            StringBuilder msg = new StringBuilder("Incompatible requirement.");
            if (method == null) {
                msg.append(requirement.getClass().getName()).append(" is not a valid AnnotatedRequirement.");
            } else {
                Class<?> methodSourceClass = method.getParameterTypes()[0];
                if (!methodSourceClass.isAssignableFrom(sourceClass)) {
                    msg.append(" Expects source type '").append(methodSourceClass.getName()).append("'.");
                }
                Class<?> methodAnnotationClass = method.getParameterTypes()[1];
                if (!methodAnnotationClass.isAssignableFrom(annotation.getClass())) {
                    msg .append(" Must be used in @").append(methodAnnotationClass.getName()).append(".")
                        .append(" Used in @").append(annotation.getClass().getName()).append(".");
                }
            }

            throw new IllegalArgumentException(msg.toString());
        }


        this.requirement_ = requirement;
        this.annotation_ = annotation;
    }


    //IMPLEMENTATION
    @Override @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean test(S s) {
        return ((AnnotatedRequirement) this.requirement_).test(s, this.annotation_);
    }
}
