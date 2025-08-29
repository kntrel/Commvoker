package com.kntrel.mc.commvoker.assembler;

import com.kntrel.mc.commvoker.argument.binding.*;
import com.kntrel.mc.commvoker.argument.context.ExecutionContext;

public class ArgumentDescriptorAssembler<S, T> implements EndAssembler<S, T> {

    //FACTORY
    public static <S, T> ArgumentDescriptorAssembler<S, T> argumentDescriptor(ArgumentDescriptor<S, T> descriptor) {
        return new ArgumentDescriptorAssembler<>(descriptor.template(), descriptor.contextualizer());
    }


    //FIELDS
    private final CommandTemplate<S> template_;
    private final Contextualizer<S, T> contextualizer_;


    //CONSTRUCTORS
    private ArgumentDescriptorAssembler(CommandTemplate<S> template, Contextualizer<S, T> contextualizer) {
        this.template_ = template;
        this.contextualizer_ = contextualizer;
    }


    //IMPLEMENTATION
    @Override
    public CommandTemplate<S> argumentTemplate() {
        return this.template_;
    }
    @Override
    public T contextualize(ExecutionContext<? extends S> context) {
        return this.contextualizer_.contextualize(context);
    }
}
