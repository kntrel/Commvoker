package com.kntrel.mc.commvoker.argument;

import com.kntrel.mc.commvoker.argument.type.VirtualArgumentType;
import com.kntrel.util.Either;
import com.mojang.brigadier.arguments.ArgumentType;
import java.util.function.Predicate;

public sealed interface ArgumentDescriptor<S> {

    // BUILDER
    static <S, T> ArgumentDescriptor.Parsed<S, T> of(ArgumentType<T> argumentType) {
        return new Parsed<>(argumentType, null);
    }
    static <S, T> ArgumentDescriptor.Parsed<S, T> of(ArgumentType<T> argumentType, Predicate<S> requirement) {
        return new Parsed<>(argumentType, requirement);
    }
    static <S, T> ArgumentDescriptor.Virtual<S, T> of(VirtualArgumentType<S, T> argumentType) {
        return new Virtual<>(argumentType, null);
    }
    static <S, T> ArgumentDescriptor.Virtual<S, T> of(VirtualArgumentType<S, T> argumentType, Predicate<S> requirement) {
        return new Virtual<>(argumentType, requirement);
    }


    // CONTRACT
    Object argumentType();
    Predicate<S> requirement();
    default Either<ArgumentType<?>, VirtualArgumentType<S, ?>> eitherType() {
        return switch (this) {
            case Parsed<S, ?> p -> Either.ofTheOne(p.argumentType());
            case Virtual<S, ?> p -> Either.ofTheOther(p.argumentType());
        };
    }


    // IMPLEMENTATIONS
    record Parsed<S, T>(ArgumentType<T> argumentType, Predicate<S> requirement) implements ArgumentDescriptor<S> {}
    record Virtual<S, T>(VirtualArgumentType<S, T> argumentType, Predicate<S> requirement) implements ArgumentDescriptor<S> {}
}
