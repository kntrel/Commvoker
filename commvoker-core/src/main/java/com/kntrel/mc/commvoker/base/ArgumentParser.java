package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.argument.type.VirtualArgumentType;
import com.mojang.brigadier.context.CommandContext;

interface ArgumentParser<S, T> {

    static <S, T> ArgumentParser<S, T> of(VirtualArgumentType<S, T> virtual) {
        return new Virtual<>(virtual);
    }
    static <S, T> ArgumentParser<S, T> of(String name, Class<T> type) {
        return new Argument<>(name, type);
    }

    T parse(CommandContext<S> ctx);

    class Virtual<S, T> implements ArgumentParser<S, T> {

        private final VirtualArgumentType<S, T> argumentType_;

        private Virtual(VirtualArgumentType<S, T> virtualArgumentType) {
            this.argumentType_ = virtualArgumentType;
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
}
