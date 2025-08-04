package com.kntrel.mc.commvoker.argument.binder;

import com.kntrel.mc.commvoker.argument.descriptor.ArgumentDescriptor;
import com.kntrel.mc.commvoker.argument.descriptor.ArgumentNode;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Predicate;

record SimpleArgumentDescriptor<S, T>(
        Collection<ArgumentNode<? super S, ?>> argumentNodes,
        BiFunction<CommandContext<S>, Object[], T> contextualizer,
        Predicate<S> requirement
) implements ArgumentDescriptor<S, T> {}
