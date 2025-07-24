package com.kntrel.mc.commvoker.argument.bind;

import com.kntrel.mc.commvoker.argument.ArgumentDescriptor;
import com.kntrel.mc.commvoker.argument.ArgumentResolutionContext;
import com.kntrel.mc.commvoker.exception.NoSuchArgumentBindingException;
import com.mojang.brigadier.arguments.ArgumentType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public class ArgumentGatherer<S> {

    private final ArgumentResolutionContext<S> context_;
    private final Set<ArgumentDescriptor<S, ?>> gathered_;

    public ArgumentGatherer(ArgumentResolutionContext<S> context) {
        this.context_ = context;
        this.gathered_ = new LinkedHashSet<>();
    }

    public ArgumentResolutionContext<S> getContext() {
        return this.context_;
    }

    public ArgumentType<?> resolveWIthType(Type type) {
        ArgumentDescriptor<S, ?> descriptor = this.context_.resolveWithType(type);
        ArgumentType<?> out = descriptor.argumentTYpe().getTheOneOrThrow(() -> new NoSuchArgumentBindingException(this.context_));
        this.gathered_.add(descriptor);
        return out;
    }

    public ArgumentType<?> resolveNext() {
        ArgumentDescriptor<S, ?> next;
        do {
            next = this.context_.resolveNext();
        } while (next.argumentTYpe().isTheOther());
        this.gathered_.add(next);
        return next.argumentTYpe().getTheOne();
    }

    Collection<ArgumentDescriptor<S, ?>> getGathered() {
        return this.gathered_;
    }

    Collection<Predicate<S>> getRequirements() {
        return this.gathered_.stream()
                .map(ArgumentDescriptor::requirement)
                .filter(Objects::nonNull)
                .toList();
    }
}
