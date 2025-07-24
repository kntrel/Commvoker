package com.kntrel.mc.commvoker.argument;

import com.kntrel.mc.commvoker.argument.type.VirtualArgumentType;
import com.kntrel.util.Either;
import com.mojang.brigadier.arguments.ArgumentType;

import java.util.function.Predicate;

public record ArgumentDescriptor<S, T>(
        Either<? extends ArgumentType<T>,? extends VirtualArgumentType<S, T>> argumentTYpe,
        Predicate<S> requirement
) {}
