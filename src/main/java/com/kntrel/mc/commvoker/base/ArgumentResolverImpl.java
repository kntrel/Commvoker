package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.argument.*;
import com.kntrel.mc.commvoker.argument.bind.ArgumentBinding;
import com.kntrel.mc.commvoker.argument.bind.ArgumentGatherer;
import com.kntrel.mc.commvoker.exception.NoSuchArgumentBindingException;
import com.kntrel.util.SetMap;
import java.lang.annotation.Annotation;
import java.util.*;

class ArgumentResolverImpl<S> implements ArgumentResolver<S>, ArgumentRegistry<S> {

    private final TreeSet<ArgumentBinding<S, ?>> unBound_;
    private final SetMap<Class<?>, ArgumentBinding<S, ?>> classMap_;
    private final SetMap<Class<? extends Annotation>, ArgumentBinding<S, ?>> annotationMap_;


    ArgumentResolverImpl() {
        this.unBound_ = new TreeSet<>();
        this.classMap_ = new SetMap<>(TreeSet::new);
        this.annotationMap_ = new SetMap<>(TreeSet::new);
    }


    @Override public void register(ArgumentBinding<S, ?> binding) {
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

    @Override public ArgumentDescriptor<S> resolve(ArgumentContext ctx) {

        PriorityQueue<ArgumentBinding<S, ?>> matches = new PriorityQueue<>();

        //Query for an exact class matching
        Class<?> type = (Class<?>) ctx.type();
        Set<ArgumentBinding<S, ?>> match = this.classMap_.get(type);
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

        ArgumentBinding<S, ?> binding = matches.poll();
        if (binding == null) {
            throw new NoSuchArgumentBindingException(ctx);
        }

        ArgumentGatherer<S> resolutionContext = new ArgumentGatherer<>(ctx, this, matches);
        return binding.descriptor(resolutionContext);
    }
}
