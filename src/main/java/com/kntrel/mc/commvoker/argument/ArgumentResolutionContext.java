package com.kntrel.mc.commvoker.argument;

import com.kntrel.mc.commvoker.argument.bind.ArgumentBinding;
import com.kntrel.mc.commvoker.exception.NoSuchArgumentBindingException;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.PriorityQueue;

public class ArgumentResolutionContext<S> extends ParameterContext {

    private final ArgumentResolver<S> argumentResolver_;
    private final PriorityQueue<ArgumentBinding<S, ?>> alsoResolved_;

    public ArgumentResolutionContext(ParameterContext delegate, ArgumentResolver<S> argumentResolver, PriorityQueue<ArgumentBinding<S, ?>> alsoResolved) {
        super(delegate);
        this.argumentResolver_ = argumentResolver;
        this.alsoResolved_ = alsoResolved;
    }


    public ArgumentResolver<S> argumentRegistry() {
        return this.argumentResolver_;
    }

    public PriorityQueue<ArgumentBinding<S, ?>> alsoResolved() {
        if (this.alsoResolved_ == null) {
            return new PriorityQueue<>();
        }
        return this.alsoResolved_;
    }

    public ArgumentDescriptor<S, ?> resolveNext() {
        if (this.alsoResolved_ == null || this.alsoResolved_.isEmpty()) {
            throw new NoSuchArgumentBindingException(this);
        }
        return this.alsoResolved_.poll().descriptor(this);
    }

    public ArgumentDescriptor<S, ?> resolveWithType(Type type) {
        ParameterContext newCtx = new ParameterContext(
                this.parameter(), type, this.method(), this.parameterIndex(), this.command(), this.commandTokenIndex()
        );
        return this.argumentResolver_.resolve(newCtx);
    }
}
