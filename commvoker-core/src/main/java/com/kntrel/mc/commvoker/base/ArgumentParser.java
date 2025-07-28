package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.argument.type.Contextualizer;
import com.kntrel.mc.commvoker.argument.type.ImplicitArgumentType;
import com.mojang.brigadier.context.CommandContext;

interface ArgumentParser<S, T> {

    static <S, T> ArgumentParser<S, T> of(ImplicitArgumentType<S, T> virtual) {
        return new Implicit<>(virtual);
    }
    static <S, T> ArgumentParser<S, T> of(String name, Class<T> type) {
        return new Argument<>(name, type);
    }
    static <S, T> ArgumentParser<S, T> of(String name, Contextualizer<S, ?, T> type) {
        return new Contextual<>(name, type);
    }

    T parse(CommandContext<S> ctx);

    class Implicit<S, T> implements ArgumentParser<S, T> {

        private final ImplicitArgumentType<S, T> argumentType_;

        private Implicit(ImplicitArgumentType<S, T> implicitArgumentType) {
            this.argumentType_ = implicitArgumentType;
        }

        @Override public T parse(CommandContext<S> ctx) {
            return this.argumentType_.parse(ctx);
        }

    }

    class Argument<S, T> implements ArgumentParser<S, T> {

        private final Class<T> type_;
        private final String name_;

        private Argument(String name, Class<T> type) {
            this.type_ = type;
            this.name_ = name;
        }

        @Override public T parse(CommandContext<S> ctx) {
            return ctx.getArgument(this.name_, this.type_);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    class Contextual<S, T> implements ArgumentParser<S, T> {

        private final String name_;
        public final Contextualizer<S, ?, T> contextualizer_;

        private Contextual(String name, Contextualizer<S, ?, T> contextualizer) {
            this.name_ = name;
            this.contextualizer_ = contextualizer;
        }

        @Override public T parse(CommandContext<S> ctx) {
            Object i = ctx.getArgument(this.name_, Object.class);
            return (T) ((Contextualizer) this.contextualizer_).contextualize(ctx, i);
        }
    }
}
