package com.kntrel.mc.commvoker.argument.binder;

import com.kntrel.mc.commvoker.argument.ArgumentContext;
import com.kntrel.mc.commvoker.argument.ArgumentResolver;
import com.kntrel.mc.commvoker.argument.ArgumentBinding;
import com.kntrel.mc.commvoker.argument.descriptor.ArgumentDescriptor;
import java.util.*;
import java.util.function.Predicate;

public class ArgumentGatherer<S> extends ArgumentContext implements ArgumentResolver<S> {

    private final ArgumentResolver<S> argumentResolver_;
    private final PriorityQueue<ArgumentBinding<? super S, ?>> alsoResolved_;
    private final Set<ArgumentDescriptor<? super S, ?>> gathered_;

    public ArgumentGatherer(ArgumentContext delegate, ArgumentResolver<S> argumentResolver, PriorityQueue<ArgumentBinding<? super S, ?>> alsoResolved) {
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
