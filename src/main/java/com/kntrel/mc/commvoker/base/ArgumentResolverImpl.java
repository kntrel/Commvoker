package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.argument.*;
import com.kntrel.mc.commvoker.argument.bind.ArgumentBinding;
import com.kntrel.mc.commvoker.argument.bind.ArgumentGatherer;
import com.kntrel.mc.commvoker.argument.bind.SimpleArgumentBinding;
import com.kntrel.mc.commvoker.argument.bind.VirtualArgumentBinding;
import com.kntrel.mc.commvoker.exception.NoSuchArgumentBindingException;
import com.kntrel.util.SetMap;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.*;

class ArgumentResolverImpl<S> implements ArgumentResolver<S>, ArgumentRegistry<S> {

    private static class Registry<C extends ParameterContext, T extends SimpleArgumentBinding<C, ?>> {
        private final TreeSet<T> unBound_;
        private final SetMap<Class<?>, T> classMap_;
        private final SetMap<Class<? extends Annotation>, T> annotationMap_;

        Registry() {
            this.unBound_ = new TreeSet<>();
            this.classMap_ = new SetMap<>(TreeSet::new);
            this.annotationMap_ = new SetMap<>(TreeSet::new);
        }

        void register(T binding) {
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

        PriorityQueue<T> resolve(C ctx) {
            PriorityQueue<T> matches = new PriorityQueue<>();

            //Query for an exact class matching
            Class<?> type = (ctx.type() instanceof ParameterizedType t)
                    ? (Class<?>) t.getRawType()
                    : (Class<?>) ctx.type();

            Set<T> match = this.classMap_.get(type);
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


    private final Registry<ArgumentContext, ArgumentBinding<S, ?>> argumentRegistry_;
    private final Registry<ParameterContext, VirtualArgumentBinding<S, ?>> virtualArgumentRegistry_;


    ArgumentResolverImpl() {
        this.argumentRegistry_ = new Registry<>();
        this.virtualArgumentRegistry_ = new Registry<>();
    }


    @Override public void register(ArgumentBinding<S, ?> binding) {
        this.argumentRegistry_.register(binding);
    }

    @Override public void register(VirtualArgumentBinding<S, ?> binding) {
        this.virtualArgumentRegistry_.register(binding);
    }

    @Override public ArgumentDescriptor.Parsed<S, ?> resolve(ArgumentContext ctx) {
        PriorityQueue<ArgumentBinding<S, ?>> matches = this.argumentRegistry_.resolve(ctx);
        ArgumentBinding<S, ?> binding = matches.poll();
        if (binding == null) {
            throw new NoSuchArgumentBindingException(ctx);
        }
        ArgumentGatherer<S> resolutionContext = new ArgumentGatherer<>(ctx, this, matches);
        return binding.descriptor(resolutionContext);
    }

    @Override public ArgumentDescriptor.Virtual<S, ?> resolveVirtual(ParameterContext ctx) {
        PriorityQueue<VirtualArgumentBinding<S, ?>> matches = this.virtualArgumentRegistry_.resolve(ctx);
        VirtualArgumentBinding<S, ?> binding = matches.poll();
        if (binding == null) {
            throw new NoSuchArgumentBindingException(ctx);
        }
        return binding.descriptor(ctx);
    }
}
