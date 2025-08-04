package com.kntrel.mc.commvoker.argument.binder;

import com.kntrel.mc.commvoker.argument.ArgumentContext;
import com.kntrel.mc.commvoker.argument.assembler.Assembler;
import com.kntrel.mc.commvoker.argument.assembler.ComposedAssembler;
import com.kntrel.mc.commvoker.argument.assembler.EndAssembler;
import com.kntrel.mc.commvoker.argument.bind.ArgumentBinding;
import com.kntrel.mc.commvoker.argument.descriptor.ArgumentDescriptor;
import com.kntrel.mc.commvoker.argument.descriptor.ArgumentNode;
import com.kntrel.util.Priority;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

record AssemblerArgumentBinding<S, T>(
        Function<ArgumentGatherer<S>, Assembler<S, T>> supplier,
        Class<T> toClass,
        Class<? extends Annotation> toAnnotation,
        Predicate<ArgumentContext> toCondition,
        Priority priority,
        Predicate<S> requirement
) implements ArgumentBinding<S, T> {

    @Override
    public ArgumentDescriptor<S, T> descriptor(ArgumentGatherer<S> ctx) {
        Assembler<S, T> assembler = this.supplier.apply(ctx);
        return new SimpleArgumentDescriptor<>(argumentNodes(assembler), new AssemblerContextualizer<>(assembler), this.requirement);
    }

    @SuppressWarnings("unchecked")
    private static <S, T> Collection<ArgumentNode<? super S, ?>> argumentNodes(Assembler<S, T> assembler) {
        List<ArgumentNode<? super S, ?>> out = new LinkedList<>();
        switch (assembler) {
            case EndAssembler<?> e -> { out.add(new ArgumentNode<>(e.argumentType(), null)); }

            case ComposedAssembler<S, T> c -> { for (var d : c.delegates()) {
                LinkedList<ArgumentNode<? super S, ?>> delegateNodes = (LinkedList<ArgumentNode<? super S,?>>) argumentNodes(d.first());
                SuggestionProvider<? super S> suggestionProvider = d.second();
                if (!delegateNodes.isEmpty() && suggestionProvider != null) {
                    ArgumentNode<? super S, ?> originalNode = delegateNodes.removeFirst();
                    delegateNodes.addFirst(new ArgumentNode<>(originalNode.argumentType(), suggestionProvider));
                }
                out.addAll(delegateNodes);
            }}

            default -> {}
        }
        return out;
    }

    private static class AssemblerContextualizer<S, T> implements BiFunction<CommandContext<S>, Object[], T> {

        private final Assembler<? super S, ? extends T> assembler_;

        AssemblerContextualizer(Assembler<? super S, ? extends T> assembler) {
            this.assembler_ = assembler;
        }

        @Override
        public T apply(CommandContext<S> ctx, Object[] objects) {
            return this.consume(ctx, new LinkedList<>(Arrays.asList(objects)), this.assembler_);
        }

        @SuppressWarnings("unchecked")
        private <I> I consume(CommandContext<S> ctx, Queue<Object> objects, Assembler<? super S, I> assembler) {

            if (assembler instanceof EndAssembler<?>) {
                return (I) objects.poll();
            }

            ComposedAssembler<? super S, I> composed = (ComposedAssembler<? super S, I>) assembler;
            var delegates = composed.delegates();
            int size = delegates.size();
            var iterator = delegates.iterator();

            Object[] objs = new Object[size];
            for (int i = 0; i < size; i++) {
                objs[i] = this.consume(ctx, objects, iterator.next().first());
            }

            return composed.contextualize(ctx, objs);
        }
    }
}