package com.kntrel.mc.commvoker.assembler;

import com.kntrel.mc.commvoker.argument.binding.*;
import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.argument.descriptor.ArgumentDescriptor;
import com.kntrel.mc.commvoker.argument.descriptor.TemplatedArgumentDescriptor;

public class ArgumentDescriptorAssembler<S, T> implements EndAssembler<S, T> {

    //FACTORY
    public static <S, T> ArgumentDescriptorAssembler<S, T> argumentDescriptor(ArgumentDescriptor<S, T> descriptor) {
        if (descriptor instanceof TemplatedArgumentDescriptor<S,T> templated) {
            return new ArgumentDescriptorAssembler<>(templated.template(), templated.contextualizer());
        }
        throw new IllegalArgumentException("Provided descriptor is not templated.");
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
    public T assemble(ExecutionContext<? extends S> context) {
        return this.contextualizer_.contextualize(context);
    }
}
