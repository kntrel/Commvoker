package com.kntrel.mc.commvoker.argument;

import com.kntrel.mc.commvoker.argument.type.ImplicitArgumentType;
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
    static <S, T> Implicit<S, T> of(ImplicitArgumentType<S, T> argumentType) {
        return new Implicit<>(argumentType, null);
    }
    static <S, T> Implicit<S, T> of(ImplicitArgumentType<S, T> argumentType, Predicate<S> requirement) {
        return new Implicit<>(argumentType, requirement);
    }


    // CONTRACT
    Object argumentType();
    Predicate<S> requirement();
    default Either<ArgumentType<?>, ImplicitArgumentType<S, ?>> eitherType() {
        return switch (this) {
            case Parsed<S, ?> p -> Either.ofTheOne(p.argumentType());
            case ArgumentDescriptor.Implicit<S, ?> p -> Either.ofTheOther(p.argumentType());
        };
    }


    // IMPLEMENTATIONS
    record Parsed<S, T>(ArgumentType<T> argumentType, Predicate<S> requirement) implements ArgumentDescriptor<S> {}
    record Implicit<S, T>(ImplicitArgumentType<S, T> argumentType, Predicate<S> requirement) implements ArgumentDescriptor<S> {}
}
