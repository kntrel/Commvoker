package com.kntrel.mc.commvoker.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.function.BiPredicate;

public interface ArgumentTypeRegistry {

    <T> void register(Class<T> type, ArgumentType<T> argumentType);
    <T> void register(Class<? extends Annotation> annotation, Class<T> type, ArgumentType<T> argumentType);
    <T> void register(Class<T> type, BiPredicate<Class<T>, Parameter> check, ArgumentType<T> argumentType);
}
