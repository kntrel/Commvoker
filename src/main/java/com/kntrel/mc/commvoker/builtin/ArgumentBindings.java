package com.kntrel.mc.commvoker.builtin;

import com.kntrel.mc.commvoker.annotation.Word;
import com.kntrel.mc.commvoker.argument.bind.UndefinedArgumentBinding;
import com.kntrel.mc.commvoker.builtin.argumentType.CollectionArgumentType;
import com.kntrel.mc.commvoker.exception.ArgumentResolutionException;
import com.mojang.brigadier.arguments.*;

import javax.lang.model.type.ArrayType;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static com.kntrel.mc.commvoker.argument.bind.ArgumentBinder.*;

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

    public static final UndefinedArgumentBinding<?>
        STRING = argument(ctx -> {
                if (ctx.isAnnotationPresent(Word.class)) { return StringArgumentType.word(); }
                if (ctx.commandTokenIndex() == ctx.command().size() - 1) { return StringArgumentType.greedyString(); }
                return StringArgumentType.string();
            })
            .toClass(String.class)
            .bind(),
        INTEGER = argument(() -> IntegerArgumentType.integer())
                .toClass(Integer.class)
                .bind(),
        LONG = argument(() -> LongArgumentType.longArg())
                .toClass(Long.class)
                .bind(),
        DOUBLE = argument(() -> DoubleArgumentType.doubleArg())
                .toClass(Double.class)
                .bind(),
        BOOLEAN = argument(BoolArgumentType::bool)
                .toClass(Boolean.class)
                .bind(),
        PRIMITIVE = compose(g -> g.resolveWIthType(boxed((Class<?>) g.getContext().type())))
                .toCondition(ctx -> ctx.type() instanceof Class<?> c && PRIMITIVES.contains(c))
                .bind(),
        LIST = compose(g -> {
                    Type type = g.getContext().type();
                    if (!(type instanceof ParameterizedType parameterizedType)) {
                        throw new ArgumentResolutionException();
                    }
                    ArgumentType<?> wrapped = g.resolveWIthType(parameterizedType.getActualTypeArguments()[0]);
                    return CollectionArgumentType.listOf(wrapped);
                })
                .toClass((Class) List.class)
                .bind(),
        SET = compose(g -> {
                Type type = g.getContext().type();
                if (!(type instanceof ParameterizedType parameterizedType)) {
                    throw new ArgumentResolutionException();
                }
                ArgumentType<?> wrapped = g.resolveWIthType(parameterizedType.getActualTypeArguments()[0]);
                return CollectionArgumentType.setOf(wrapped);
            })
                    .toClass((Class) Set.class)
                    .bind();


}
