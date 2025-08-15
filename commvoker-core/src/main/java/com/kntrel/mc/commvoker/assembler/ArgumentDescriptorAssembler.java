package com.kntrel.mc.commvoker.assembler;

import com.kntrel.mc.commvoker.argument.binding.ArgumentDescriptor;
import com.kntrel.mc.commvoker.argument.binding.ArgumentNode;
import com.kntrel.util.tuple.Pair;
import com.kntrel.util.tuple.impl.SimplePair;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class ArgumentDescriptorAssembler<S, T> implements ComposedAssembler<S, T> {

    //FACTORY
    public static <S, T> ArgumentDescriptorAssembler<S, T> argumentDescriptor(ArgumentDescriptor<S, T> argumentDescriptor) {
        return new ArgumentDescriptorAssembler<>(argumentDescriptor);
    }


    //FIELDS
    private final List<Pair<Assembler<? super S, ?>, SuggestionProvider<? super S>>> delegates_;
    private final BiFunction<CommandContext<? extends S>, Object[], T> contextualizer_;


    //CONSTRUCTOR
    private ArgumentDescriptorAssembler(ArgumentDescriptor<S, T> argumentDescriptor) {
        this.delegates_ = new ArrayList<>();
        for (ArgumentNode<? super S, ?> node : argumentDescriptor.argumentTrees()) {
            Assembler<? super S, ?> assembler = Assembler.ofArgumentType(node.argumentType());
            SuggestionProvider<? super S> suggestionProvider = node.suggestionProvider();
            Pair<Assembler<? super S, ?>, SuggestionProvider<? super S>> pair = new SimplePair<>(assembler, suggestionProvider);
            this.delegates_.add(pair);
        }
        this.contextualizer_ = argumentDescriptor.contextualizer();
    }


    //IMPLEMENTATION
    @Override
    public List<Pair<Assembler<? super S, ?>, SuggestionProvider<? super S>>> composedOf() {
        return this.delegates_;
    }
    @Override
    public T compose(CommandContext<? extends S> ctx, Object[] objects) {
        return this.contextualizer_.apply(ctx, objects);
    }
}
