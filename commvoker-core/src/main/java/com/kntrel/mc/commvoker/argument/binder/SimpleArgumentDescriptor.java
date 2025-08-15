package com.kntrel.mc.commvoker.argument.binder;

import com.kntrel.mc.commvoker.argument.binding.ArgumentDescriptor;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Predicate;

record SimpleArgumentDescriptor<S, T>(
        Collection<ArgumentBuilder<? super S, ?>> argumentNodes,
        BiFunction<CommandContext<? extends S>, Object[], T> contextualizer,
        Predicate<S> requirement
) implements ArgumentDescriptor<S, T> {}
