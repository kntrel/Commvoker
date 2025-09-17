package com.kntrel.mc.commvoker.argument.context;

import com.kntrel.mc.commvoker.argument.ArgumentResolver;
import com.kntrel.mc.commvoker.argument.binding.ArgumentBinding;
import com.kntrel.mc.commvoker.argument.descriptor.ArgumentDescriptor;
import com.kntrel.mc.commvoker.exception.NoSuchArgumentBindingException;
import java.lang.reflect.Type;
import java.util.*;

public class ArgumentGatherer<S> extends ArgumentContext {

    private final ArgumentResolver<S> argumentResolver_;
    private final PriorityQueue<ArgumentBinding.Descriptive<? super S, ?>> alsoResolved_;
    private final Set<ArgumentDescriptor<? super S, ?>> gathered_;

    public ArgumentGatherer(ArgumentContext delegate, ArgumentResolver<S> argumentResolver, PriorityQueue<ArgumentBinding.Descriptive<? super S, ?>> alsoResolved) {
        super(delegate);
        this.argumentResolver_ = argumentResolver;
        this.alsoResolved_ = alsoResolved;
        this.gathered_ = new LinkedHashSet<>();
    }

    public ArgumentDescriptor<? super S, ?> resolve(Type type) {
        if (this.type().equals(type)) {
            return resolveNext();
        }

        ArgumentContext newContext = new ArgumentContext(this.commandHolder(), this.parameter(), type, this.method(), this.parameterIndex(), this.command(), this.parameterIndex(), this.previous());
        ArgumentDescriptor<? super S, ?> descriptor = this.argumentResolver_.resolve(newContext);
        this.gathered_.add(descriptor);
        return descriptor;
    }

    public ArgumentDescriptor<? super S, ?> resolveNext() {
        ArgumentBinding.Descriptive<? super S, ?> binding = this.alsoResolved_.poll();
        if (binding == null) {
            throw new NoSuchArgumentBindingException(this);
        }
        ArgumentDescriptor<? super S, ?> descriptor = binding.descriptor(this);
        this.gathered_.add(descriptor);
        return descriptor;
    }



    Collection<ArgumentDescriptor<? super S, ?>> getGathered() {
        return this.gathered_;
    }
}
