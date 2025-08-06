package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.argument.*;
import com.kntrel.mc.commvoker.argument.ArgumentBinding;
import com.kntrel.mc.commvoker.argument.binder.ArgumentGatherer;
import com.kntrel.mc.commvoker.argument.descriptor.ArgumentDescriptor;
import com.kntrel.mc.commvoker.exception.NoSuchArgumentBindingException;
import com.kntrel.util.SetMap;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.*;

class ArgumentResolverImpl<S> implements ArgumentResolver<S>, ArgumentRegistry<S> {

    private static class Registry<S> {
        private final TreeSet<ArgumentBinding<? super S, ?>> unBound_;
        private final SetMap<Class<?>, ArgumentBinding<? super S, ?>> classMap_;
        private final SetMap<Class<? extends Annotation>, ArgumentBinding<? super S, ?>> annotationMap_;

        Registry() {
            this.unBound_ = new TreeSet<>();
            this.classMap_ = new SetMap<>(TreeSet::new);
            this.annotationMap_ = new SetMap<>(TreeSet::new);
        }

        void register(ArgumentBinding<? super S, ?> binding) {
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

        PriorityQueue<ArgumentBinding<? super S, ?>> resolve(ArgumentContext ctx) {
            PriorityQueue<ArgumentBinding<? super S, ?>> matches = new PriorityQueue<>();

            //Query for an exact class matching
            Class<?> type = (ctx.type() instanceof ParameterizedType t)
                    ? (Class<?>) t.getRawType()
                    : (Class<?>) ctx.type();

            Set<ArgumentBinding<? super S, ?>> match = this.classMap_.get(type);
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


    private final Registry<S> registry_;


    ArgumentResolverImpl() {
        this.registry_ = new Registry<>();
    }


    @Override public void register(ArgumentBinding<? super S, ?> binding) {
        this.registry_.register(binding);
    }


    @Override public ArgumentDescriptor<? super S, ?> resolve(ArgumentContext ctx) {
        PriorityQueue<ArgumentBinding<? super S, ?>> matches = this.registry_.resolve(ctx);
        ArgumentBinding<? super S, ?> binding = matches.poll();
        if (binding == null) {
            throw new NoSuchArgumentBindingException(ctx);
        }
        ArgumentGatherer<S> resolutionContext = new ArgumentGatherer<>(ctx, this, matches);
        return binding.descriptor(resolutionContext);
    }
}
