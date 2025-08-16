package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.argument.*;
import com.kntrel.mc.commvoker.argument.binding.ArgumentBinding;
import com.kntrel.mc.commvoker.argument.context.ArgumentGatherer;
import com.kntrel.mc.commvoker.argument.binding.ArgumentDescriptor;
import com.kntrel.mc.commvoker.argument.context.ArgumentContext;
import com.kntrel.mc.commvoker.argument.context.ParameterContext;
import com.kntrel.mc.commvoker.exception.NoSuchArgumentBindingException;
import com.kntrel.util.SetMap;
import com.mojang.brigadier.context.CommandContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Function;

class ArgumentResolverImpl<S> implements ArgumentResolver<S>, ArgumentRegistry<S> {

    private static class Registry<S, C extends ParameterContext, B extends ArgumentBinding<? super S, C, ?>> {
        private final TreeSet<B> unBound_;
        private final SetMap<Class<?>, B> classMap_;
        private final SetMap<Class<? extends Annotation>, B> annotationMap_;

        Registry() {
            this.unBound_ = new TreeSet<>();
            this.classMap_ = new SetMap<>(TreeSet::new);
            this.annotationMap_ = new SetMap<>(TreeSet::new);
        }

        void register(B binding) {
            if (binding.toClass() != null) {
                this.classMap_.putInto(binding.toClass(), binding);
                return;
            }
            if (binding.toAnnotation() != null) {
                this.annotationMap_.putInto(binding.toAnnotation(), binding);
                return;
            }
            this.unBound_.add(binding);
        }

        PriorityQueue<B> resolve(C ctx) {
            PriorityQueue<B> matches = new PriorityQueue<>();

            //Query for an exact class matching
            Class<?> type = (ctx.type() instanceof ParameterizedType t)
                    ? (Class<?>) t.getRawType()
                    : (Class<?>) ctx.type();

            Set<B> match = this.classMap_.get(type);
            if (match != null) {
                match.stream()
                        .filter(b -> b.test(ctx))
                        .forEach(matches::add);
            }

            //Query superclass matches
            if (matches.isEmpty()) {
                this.classMap_.entrySet().stream()
                        .filter(e -> e.getKey().isAssignableFrom(type))
                        .flatMap(e -> e.getValue().stream())
                        .filter(b -> b.test(ctx))
                        .forEach(matches::add);
            }

            //Query annotation-bound matches
            for (Annotation ann : ctx.parameter().getAnnotations()) {
                match = this.annotationMap_.get(ann.getClass());
                if (match == null) { continue; }
                match.stream()
                        .filter(b -> b.test(ctx))
                        .forEach(matches::add);
            }

            //Query unbound binding
            this.unBound_.stream()
                    .filter(b -> b.test(ctx))
                    .forEach(matches::add);

            //Handle no matches
            if (matches.isEmpty()) {
                throw new NoSuchArgumentBindingException(ctx);
            }

            return matches;
        }
    }


    private final Registry<S, ParameterContext, ArgumentBinding.Implicit<? super S, ?>> registryImplicit_;
    private final Registry<S, ArgumentContext, ArgumentBinding.Descriptive<? super S, ?>> registryDescriptive_;


    ArgumentResolverImpl() {
        this.registryImplicit_ = new Registry<>();
        this.registryDescriptive_ = new Registry<>();
    }


    @Override @SuppressWarnings("unchecked")
    public void register(ArgumentBinding<? super S, ? extends ParameterContext, ?> binding) {
        switch (binding) {
            case ArgumentBinding.Implicit<?, ?> imp -> this.registryImplicit_.register((ArgumentBinding.Implicit<? super S, ?>) imp);
            case ArgumentBinding.Descriptive<?, ?> desc -> this.registryDescriptive_.register((ArgumentBinding.Descriptive<? super S, ?>) desc);
        };
    }


    @Override public ArgumentDescriptor<? super S, ?> resolve(ArgumentContext ctx) {
        PriorityQueue<ArgumentBinding.Descriptive<? super S, ?>> matches = this.registryDescriptive_.resolve(ctx);
        ArgumentBinding.Descriptive<? super S, ?> binding = matches.poll();
        if (binding == null) {
            throw new NoSuchArgumentBindingException(ctx);
        }
        ArgumentGatherer<S> resolutionContext = new ArgumentGatherer<>(ctx, this, matches);
        return binding.descriptor(resolutionContext);
    }

    @Override @SuppressWarnings("unchecked")
    public Function<CommandContext<? extends S>, ?> resolve(ParameterContext ctx) {
        PriorityQueue<ArgumentBinding.Implicit<? super S, ?>> matches = this.registryImplicit_.resolve(ctx);
        ArgumentBinding.Implicit<? super S, ?> binding = matches.poll();
        if (binding == null) {
            throw new NoSuchArgumentBindingException(ctx);
        }
        return (Function<CommandContext<? extends S>, ?>) binding.implyer();
    }
}
