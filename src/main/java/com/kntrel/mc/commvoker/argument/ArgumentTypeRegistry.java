package com.kntrel.mc.commvoker.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.function.BiPredicate;

public interface ArgumentTypeRegistry<S> {

    <T> void register(Class<T> type, ParameterPredicate<T> check, ArgumentType<T> argumentType);
    <T> void register(Class<T> type, ParameterPredicate<T> check, VirtualArgumentType<S ,T> argumentType);

    default  <T> void register(Class<T> type, ArgumentType<T> argumentType) {
        this.register(type, null, argumentType); 
    }
    default <T> void register(Class<? extends Annotation> annotation, Class<T> type, ArgumentType<T> argumentType) {
        this.register(type, (t,p,m) -> p.isAnnotationPresent(annotation), argumentType);
    }

    default <T> void register(Class<T> type, VirtualArgumentType<S, T> argumentType) {
        this.register(type, null, argumentType);
    }
    default <T> void register(Class<? extends Annotation> annotation, Class<T> type, VirtualArgumentType<S, T> argumentType) {
        this.register(type, (t,p,m) -> p.isAnnotationPresent(annotation), argumentType);
    }
    default <T> void register(ArgumentTypeRegistration<S, T> registration) {
        if (registration.virtualArgumentType == null) {
            this.register(registration.type, registration.predicate, registration.argumentType);
        } else {
            this.register(registration.type, registration.predicate, registration.virtualArgumentType);
        }
    }
}
