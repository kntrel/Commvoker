package com.kntrel.mc.commvoker.assembler;

import com.kntrel.mc.commvoker.argument.binding.*;
import com.mojang.brigadier.context.CommandContext;

public class ArgumentDescriptorAssembler<S, T> implements EndAssembler<S, T> {

    //FACTORY
    public static <S, T> ArgumentDescriptorAssembler<S, T> argumentDescriptor(ArgumentDescriptor<S, T> descriptor) {
        return new ArgumentDescriptorAssembler<>(descriptor.argumentTrees(), descriptor.contextualizer());
    }


    //FIELDS
    private final CommandTemplate.Node<S> template_;
    private final Contextualizer<S, T> contextualizer_;


    //CONSTRUCTORS
    private ArgumentDescriptorAssembler(CommandTemplate.Node<S> template, Contextualizer<S, T> contextualizer) {
        this.template_ = template;
        this.contextualizer_ = contextualizer;
    }


    //IMPLEMENTATION
    @Override
    public CommandTemplate.Node<S> argumentTemplate() {
        return this.template_;
    }
    @Override
    public T contextualize(CommandContext<? extends S> context, Components components) {
        return this.contextualizer_.contextualize(context, components);
    }
}
