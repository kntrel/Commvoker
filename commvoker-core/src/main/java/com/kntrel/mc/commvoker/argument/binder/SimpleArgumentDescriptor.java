package com.kntrel.mc.commvoker.argument.binder;

import com.kntrel.mc.commvoker.argument.binding.ArgumentDescriptor;
import com.kntrel.mc.commvoker.argument.binding.CommandTemplate;
import com.kntrel.mc.commvoker.argument.binding.Contextualizer;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Predicate;

record SimpleArgumentDescriptor<S, T>(
        CommandTemplate.Node<S> argumentTrees,
        Contextualizer<S, T> contextualizer,
        Predicate<S> requirement
) implements ArgumentDescriptor<S, T> {}
