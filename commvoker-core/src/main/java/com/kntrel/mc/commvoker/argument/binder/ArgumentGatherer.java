package com.kntrel.mc.commvoker.argument.binder;

import com.kntrel.mc.commvoker.argument.ArgumentContext;
import com.kntrel.mc.commvoker.argument.ArgumentResolver;
import com.kntrel.mc.commvoker.argument.ParameterContext;
import com.kntrel.mc.commvoker.argument.bind.ArgumentBinding;
import com.kntrel.mc.commvoker.argument.descriptor.ArgumentDescriptor;
import com.kntrel.mc.commvoker.exception.NoSuchArgumentBindingException;
import com.mojang.brigadier.arguments.ArgumentType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;

public class ArgumentGatherer<S> extends ArgumentContext implements ArgumentResolver<S> {

    private final ArgumentResolver<S> argumentResolver_;
    private final PriorityQueue<ArgumentBinding<S, ?>> alsoResolved_;
    private final Set<ArgumentDescriptor<? super S, ?>> gathered_;

    public ArgumentGatherer(ArgumentContext delegate, ArgumentResolver<S> argumentResolver, PriorityQueue<ArgumentBinding<S, ?>> alsoResolved) {
        super(delegate);
        this.argumentResolver_ = argumentResolver;
        this.alsoResolved_ = alsoResolved;
        this.gathered_ = new LinkedHashSet<>();
    }

    @Override public ArgumentDescriptor<? super S, ?> resolve(ArgumentContext ctx) {
        ArgumentDescriptor<? super S, ?> result = this.argumentResolver_.resolve(ctx);
        this.gathered_.add(result);
        return result;
    }

    @Override
    public ArgumentDescriptor<? super S, ?> resolveImplicit(ParameterContext ctx) {
        return this.argumentResolver_.resolveImplicit(ctx);
    }

    public ArgumentType<?> resolveType(Type type) {
        if (type.equals(this.type())) {
            return this.resolveNextType();
        }

        ArgumentContext ctx = new ArgumentContext(this.parameter(), type, this.method(), this.parameterIndex(), this.command(), this.commandTokenIndex(), this.previousTypes());

        ArgumentDescriptor<? super S, ?> descriptor = this.resolve(ctx);
        ArgumentType<?> result = descriptor.argumentNodes().iterator().next().argumentType();

        this.gathered_.add(descriptor);
        return result;
    }

    public ArgumentType<?> resolveNextType() {
        ArgumentDescriptor<? super S, ?> next;
        do {
            ArgumentBinding<S, ?> binding = this.alsoResolved_.poll();
            if (binding == null) {
                throw new NoSuchArgumentBindingException(this);
            }
            next = binding.descriptor(this);
        } while (next != null);
        this.gathered_.add(next);
        return next.argumentNodes().iterator().next().argumentType();
    }

    Collection<ArgumentDescriptor<? super S, ?>> getGathered() {
        return this.gathered_;
    }

    Collection<? extends Predicate<? super S>> getRequirements() {
        return this.gathered_.stream()
                .map(ArgumentDescriptor::requirement)
                .filter(Objects::nonNull)
                .toList();
    }
}
