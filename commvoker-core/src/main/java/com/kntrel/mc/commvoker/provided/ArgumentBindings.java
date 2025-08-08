package com.kntrel.mc.commvoker.provided;

import com.kntrel.mc.commvoker.argument.binder.ArgumentGatherer;
import com.kntrel.mc.commvoker.provided.annotations.Max;
import com.kntrel.mc.commvoker.provided.annotations.Min;
import com.kntrel.mc.commvoker.provided.annotations.NotGreedy;
import com.kntrel.mc.commvoker.provided.annotations.Word;
import com.kntrel.mc.commvoker.argument.descriptor.ArgumentDescriptor;
import com.kntrel.mc.commvoker.assembler.ArgumentDescriptorAssembler;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.argument.ArgumentBinding;
import com.kntrel.mc.commvoker.command.CommandPattern;
import com.kntrel.mc.commvoker.command.CommandPatternToken;
import com.kntrel.mc.commvoker.provided.assemblers.*;
import com.kntrel.util.Constants;
import com.kntrel.util.Priority;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.kntrel.mc.commvoker.argument.binder.ArgumentBinder.*;

public final class ArgumentBindings {
    private ArgumentBindings() {}

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
    private static <T extends Number, A extends Assembler<?, T>> A rangeNumber(
            BiFunction<T, T, A> builder,
            Function<Double, T> translator,
            T min,
            T max,
            ArgumentGatherer<?> context
    ) {
        if (context.isAnnotationPresent(Min.class)) { min = translator.apply(context.getAnnotation(Min.class).value()); }
        if (context.isAnnotationPresent(Max.class)) { max = translator.apply(context.getAnnotation(Max.class).value()); }
        return builder.apply(min, max);
    }


    public static final ArgumentBinding<Object, ?>
        STRING = argumentAssembler(ctx -> {
                if (ctx.isAnnotationPresent(Word.class)) { return StringAssembler.word(); }

                CommandPattern command = ctx.command();
                CommandPatternToken latestToken = command.getTokenAt(command.size() - 1);
                if (
                        ctx.parameterIndex() == ctx.method().getParameterCount() - 1
                     && !latestToken.isLiteral()
                     && ctx.parameter().getParameterizedType() instanceof Class<?>
                     && !ctx.isAnnotationPresent(NotGreedy.class)
                ) { return StringAssembler.greedyString(); }
                return StringAssembler.string();
            })
            .toClass(String.class)
            .withPriority(Priority.LOW)
            .bind(),
        INTEGER = argumentAssembler(ctx -> rangeNumber(
                IntegerAssembler::integer,
                Double::intValue,
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                ctx
            ))
            .toClass(Integer.class)
            .withPriority(Priority.LOW)
            .bind(),
        LONG = argumentAssembler(ctx -> rangeNumber(
                LongAssembler::longArg,
                Double::longValue,
                Long.MIN_VALUE,
                Long.MAX_VALUE,
                ctx
            ))
            .toClass(Long.class)
            .withPriority(Priority.LOW)
            .bind(),
        DOUBLE = argumentAssembler(ctx -> rangeNumber(
                DoubleAssembler::doubleArg,
                Function.identity(),
                Double.MIN_VALUE,
                Double.MAX_VALUE,
                ctx
            ))
            .toClass(Double.class)
            .withPriority(Priority.LOW)
            .bind(),
        BOOLEAN = argumentAssembler(BoolAssembler::bool)
            .toClass(Boolean.class)
            .withPriority(Priority.LOW)
            .bind(),
        PRIMITIVE = argumentAssembler(ctx -> {
                ArgumentDescriptor<Object, ?> descriptor = (ArgumentDescriptor<Object, ?>) ctx.resolve(boxed((Class<?>) ctx.type()));
                return ArgumentDescriptorAssembler.argumentDescriptor(descriptor);
            })
            .toCondition(ctx -> ctx.type() instanceof Class<?> c && PRIMITIVES.contains(c))
            .withPriority(Priority.LOW)
            .bind(),
        LIST = argumentAssembler(ctx -> {
                ParameterizedType parameterizedType = (ParameterizedType) ctx.type();
                ArgumentDescriptor<?, ?> descriptor = ctx.resolve(parameterizedType.getActualTypeArguments()[0]);
                return (Assembler<Object, List<?>>) (Assembler<?, ?>) CollectionAssembler.listOf(Assembler.ofArgumentDescriptor(descriptor));
            })
            .toClass((Class<List<?>>) (Class) List.class)
            .toCondition(ctx -> ctx.type() instanceof ParameterizedType)
            .bind(),
        SET = argumentAssembler(ctx -> {
                ParameterizedType parameterizedType = (ParameterizedType) ctx.type();
                ArgumentDescriptor<?, ?> descriptor = ctx.resolve(parameterizedType.getActualTypeArguments()[0]);
                return (Assembler<Object, Set<?>>) (Assembler<?, ?>) CollectionAssembler.setOf(Assembler.ofArgumentDescriptor(descriptor));
            })
            .toClass((Class<Set<?>>) (Class) Set.class)
            .toCondition(ctx -> ctx.type() instanceof ParameterizedType)
            .bind(),
        ARRAY = argumentAssembler(ctx -> {
                Class<?> type = ((Class<?>) ctx.type()).componentType();
                ArgumentDescriptor<?, ?> descriptor = ctx.resolve(type);
                return ArrayAssembler.arrayOf((Class<Object>) type, (Assembler<?, Object>) Assembler.ofArgumentDescriptor(descriptor));
            })
            .toCondition(ctx -> ctx.type() instanceof Class<?> c && c.isArray())
            .bind();


    @SuppressWarnings("unchecked")
    public static Collection<ArgumentBinding<Object, ?>> all() {
        return Constants.getAll(ArgumentBindings.class, (Class<ArgumentBinding<Object, ?>>) (Class<?>) ArgumentBinding.class);
    }
}