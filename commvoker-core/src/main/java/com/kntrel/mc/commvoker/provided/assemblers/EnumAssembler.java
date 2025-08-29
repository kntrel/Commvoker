package com.kntrel.mc.commvoker.provided.assemblers;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.TransformAssembler;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EnumAssembler<T extends Enum<T>> implements TransformAssembler<Object, String, T> {

    //FACTORY
    private static final ClassValue<EnumAssembler<?>> CACHE = new ClassValue<>() {
        @Override @SuppressWarnings({"rawtypes", "unchecked"})
        protected EnumAssembler<?> computeValue(Class<?> type) {
            Class<? extends Enum> ec = type.asSubclass(Enum.class);
            return new EnumAssembler<>(ec);
        }
    };

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> EnumAssembler<T> ofEnum(Class<T> enumClass) {
        return (EnumAssembler<T>) CACHE.get(enumClass);
    }


    //FIELDS
    private final Class<T> enum_;
    private final List<String> names_;


    //CONSTRUCTOR
    private EnumAssembler(Class<T> enumClass) {
        this.enum_ = enumClass;
        this.names_ = EnumSet.allOf(this.enum_).stream().map(Enum::name).toList();
    }


    //IMPLEMENTATION
    @Override
    public Assembler<? super Object, ? extends String> delegate() {
        return StringAssembler.word();
    }
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<Object> context, SuggestionsBuilder builder) {
        this.names_.forEach(builder::suggest);
        return builder.buildFuture();
    }
    @Override
    public T compose(ExecutionContext<?> ctx, String object) {
        return Enum.valueOf(this.enum_, object);
    }
}