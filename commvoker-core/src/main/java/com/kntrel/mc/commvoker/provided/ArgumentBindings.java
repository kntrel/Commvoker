package com.kntrel.mc.commvoker.provided;

import com.kntrel.mc.commvoker.annotation.Word;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.argument.ArgumentBinding;
import com.kntrel.mc.commvoker.command.CommandPattern;
import com.kntrel.mc.commvoker.command.CommandPatternToken;
import com.kntrel.mc.commvoker.provided.assemblers.*;
import com.kntrel.util.Constants;

import java.lang.reflect.Parameter;
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

                CommandPattern command = ctx.command();
                CommandPatternToken latestToken = command.getTokenAt(command.size() - 1);
                if (ctx.method().getParameterCount() == ctx.parameterIndex() - 1 && !latestToken.isLiteral()) { return StringAssembler.greedyString(); }
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
        DOUBLE = argumentAssembler(() -> DoubleAssembler.doubleArg())
                .toClass(Double.class)
                .bind(),
        BOOLEAN = argumentAssembler(BoolAssembler::bool)
                .toClass(Boolean.class)
                .bind();


    @SuppressWarnings("unchecked")
    public static Collection<ArgumentBinding<Object, ?>> all() {
        return Constants.getAll(ArgumentBindings.class, (Class<ArgumentBinding<Object, ?>>) (Class<?>) ArgumentBindings.class);
    }
}
