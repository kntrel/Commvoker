package com.kntrel.mc.commvoker.argument;

import com.mojang.brigadier.arguments.ArgumentType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class ArgumentTypeRegistration<S, T> {

    public static <S, T> ArgumentTypeRegistration<S, T> of(Class<T> clazz, ParameterPredicate<T> predicate, ArgumentType<T> argumentType) {
        return new ArgumentTypeRegistration<>(clazz, predicate, argumentType, null);
    }
    public static <S, T> ArgumentTypeRegistration<S, T> of(Class<T> clazz, ParameterPredicate<T> predicate, VirtualArgumentType<S, T> argumentType) {
        return new ArgumentTypeRegistration<>(clazz, predicate, null, argumentType);
    }
    public static <S, T> ArgumentTypeRegistration<S, T> of(Class<T> clazz, ArgumentType<T> argumentType) {
        return new ArgumentTypeRegistration<>(clazz, null, argumentType, null);
    }
    public static <S, T> ArgumentTypeRegistration<S, T> of(Class<T> clazz, VirtualArgumentType<S, T> argumentType) {
        return new ArgumentTypeRegistration<>(clazz, null, null, argumentType);
    }
    public static <S, T> ArgumentTypeRegistration<S, T> of(Class<? extends Annotation> annotation, Class<T> clazz, ArgumentType<T> argumentType) {
        return new ArgumentTypeRegistration<>(clazz, new AnnotationCHeck<>(annotation), argumentType, null);
    }
    public static <S, T> ArgumentTypeRegistration<S, T> of(Class<? extends Annotation> annotation, Class<T> clazz, VirtualArgumentType<S, T> argumentType) {
        return new ArgumentTypeRegistration<>(clazz, new AnnotationCHeck<>(annotation), null, argumentType);
    }


    protected final Class<T> type;
    protected final ParameterPredicate<T> predicate;
    protected final ArgumentType<T> argumentType;
    protected final VirtualArgumentType<S, T> virtualArgumentType;


    private ArgumentTypeRegistration(Class<T> clazz, ParameterPredicate<T> predicate, ArgumentType<T> argumentType, VirtualArgumentType<S, T> virtualArgumentType) {
        this.type = clazz;
        this.predicate = predicate;
        this.argumentType = argumentType;
        this.virtualArgumentType = virtualArgumentType;
    }


    private static class AnnotationCHeck<T> implements ParameterPredicate<T> {

        private final Class<? extends Annotation> annotationClass_;
        AnnotationCHeck(Class<? extends Annotation> annotationClass) { this.annotationClass_ = annotationClass; }

        @Override
        public boolean test(Class<T> tClass, Parameter parameter, Method m) { return parameter.isAnnotationPresent(annotationClass_); }
    }
}
