package com.kntrel.mc.commvoker.argument.binder;

import com.kntrel.mc.commvoker.argument.ArgumentContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.ComposedAssembler;
import com.kntrel.mc.commvoker.assembler.EndAssembler;
import com.kntrel.mc.commvoker.argument.ArgumentBinding;
import com.kntrel.mc.commvoker.argument.descriptor.ArgumentDescriptor;
import com.kntrel.mc.commvoker.argument.descriptor.ArgumentNode;
import com.kntrel.util.Priority;
import com.kntrel.util.tuple.Pair;
import com.kntrel.util.tuple.impl.SimplePair;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

record AssemblerArgumentBinding<S, T>(
        Function<ArgumentGatherer<? extends S>, Assembler<S, T>> supplier,
        Class<T> toClass,
        Class<? extends Annotation> toAnnotation,
        Predicate<ArgumentContext> toCondition,
        Priority priority,
        Predicate<S> requirement
) implements ArgumentBinding<S, T> {

    @Override @SuppressWarnings("unchecked")
    public ArgumentDescriptor<S, T> descriptor(ArgumentGatherer<? extends S> ctx) {
        Assembler<S, T> assembler = this.supplier.apply(ctx);
        BiFunction<CommandContext<? extends S>, Object[], T> contextualizer = switch (assembler) {
            case EndAssembler<?> end -> (c, o) -> (T) o[0];
            case ComposedAssembler<? super S, T> comp -> comp::contextualize;
        };
        return new SimpleArgumentDescriptor<>(argumentNodes(assembler), contextualizer, this.requirement);
    }


    private static <S> List<ArgumentNode<? super S, ?>> argumentNodes(Assembler<? super S, ?> root) {
        Deque<Pair<Assembler<? super S, ?>, SuggestionProvider<? super S>>> stack = new ArrayDeque<>();
        stack.addLast(new SimplePair<>(root, null));

        List<ArgumentNode<? super S, ?>> out = new ArrayList<>();

        while (!stack.isEmpty()) {
            var pair = stack.pollLast();
            var asm  = pair.first();
            var sugg = pair.second();

            switch (asm) {
                case EndAssembler<?> end -> out.add(new ArgumentNode<>((ArgumentType<?>) end.argumentType(), sugg));
                case ComposedAssembler<? super S, ?> comp -> {
                    var delegates = comp.delegates();
                    for (int i = delegates.size() - 1; i >= 0; i--) {
                        var d = delegates.get(i);
                        stack.addLast(new SimplePair<>(d.first(), d.second()));
                    }
                }
            }
        }
        return out;
    }
}