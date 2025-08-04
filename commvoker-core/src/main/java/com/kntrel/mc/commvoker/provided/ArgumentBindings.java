package com.kntrel.mc.commvoker.provided;

import com.kntrel.mc.commvoker.annotation.Word;
import com.kntrel.mc.commvoker.argument.bind.ArgumentBinding;
import com.kntrel.mc.commvoker.provided.argumentType.CollectionArgumentType;
import com.kntrel.mc.commvoker.exception.ArgumentResolutionException;
import com.kntrel.mc.commvoker.provided.assemblers.IntegerAssembler;
import com.kntrel.mc.commvoker.provided.assemblers.LongAssembler;
import com.kntrel.mc.commvoker.provided.assemblers.StringAssembler;
import com.kntrel.util.Constants;
import com.mojang.brigadier.arguments.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static com.kntrel.mc.commvoker.argument.binder.ArgumentBinder.*;

public final class ArgumentBindings {



    private static final Set<Class<?>> PRIMITIVES = Set.of(boolean.class, byte.class, short.class, int.class, long.class, float.class, double.class);
    private static Class<?> boxed(Class<?> primitive) {
        if (primitive.equals(boolean.class)) {
            return Boolean.class;
        }
        if (primitive.equals(float.class) || primitive.equals(double.class)) {
            return Double.class;
        }
        if (primitive.equals(long.class)) {
            return Long.class;
        }
        return Integer.class;
    }

    private ArgumentBindings() {}

    public static final ArgumentBinding<Object, ?>
        STRING = argumentAssembler(ctx -> {
                if (ctx.isAnnotationPresent(Word.class)) { return StringAssembler.word(); }
                if (ctx.commandTokenIndex() == ctx.command().size() - 1) { return StringAssembler.greedyString(); }
                return StringAssembler.string();
            })
            .toClass(String.class)
            .bind(),
        INTEGER = argumentAssembler(() -> IntegerAssembler.integer())
                .toClass(Integer.class)
                .bind(),
        LONG = argumentAssembler(() -> LongAssembler.longArg())
                .toClass(Long.class)
                .bind(),
        DOUBLE = argumentAssembler(() -> DoubleArgumentType.doubleArg())
                .toClass(Double.class)
                .bind(),
        BOOLEAN = argumentAssembler(BoolArgumentType::bool)
                .toClass(Boolean.class)
                .bind(),
        PRIMITIVE = argumentAssembler(g -> g.resolveType(boxed((Class<?>) g.type())))
                .toCondition(ctx -> ctx.type() instanceof Class<?> c && PRIMITIVES.contains(c))
                .bind(),
        LIST = argumentAssembler(g -> {
                    Type type = g.type();
                    if (!(type instanceof ParameterizedType parameterizedType)) {
                        throw new ArgumentResolutionException();
                    }
                    ArgumentType<?> wrapped = g.resolveType(parameterizedType.getActualTypeArguments()[0]);
                    return CollectionArgumentType.listOf(wrapped);
                })
                .toClass((Class) List.class)
                .bind(),
        SET = argumentAssembler(g -> {
                Type type = g.type();
                if (!(type instanceof ParameterizedType parameterizedType)) {
                    throw new ArgumentResolutionException();
                }
                ArgumentType<?> wrapped = g.resolveType(parameterizedType.getActualTypeArguments()[0]);
                return CollectionArgumentType.setOf(wrapped);
            })
                    .toClass((Class) Set.class)
                    .bind();


    @SuppressWarnings("unchecked")
    public static Collection<ArgumentBinding<Object, ?>> all() {
        return Constants.getAll(ArgumentBindings.class, (Class<ArgumentBinding<Object, ?>>) (Class<?>) ArgumentBindings.class);
    }
}
