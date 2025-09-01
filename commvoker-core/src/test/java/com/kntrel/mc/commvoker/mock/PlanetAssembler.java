package com.kntrel.mc.commvoker.mock;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.TransformAssembler;
import com.kntrel.mc.commvoker.provided.assemblers.StringAssembler;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class PlanetAssembler implements TransformAssembler<Object, String, Planet> {

    private final Consumer<ExecutionContext<?>> suggestionContextCallback;

    public PlanetAssembler(Consumer<ExecutionContext<?>> suggestionCallback) {
        this.suggestionContextCallback = suggestionCallback;
    }

    @Override
    public Assembler<? super Object, ? extends String> delegate() {
        return StringAssembler.word();
    }

    @Override
    public Planet compose(ExecutionContext<?> ctx, String object) {
        return Arrays.stream(Planet.values())
                .filter(p -> p.getName().equalsIgnoreCase(object))
                .findFirst()
                .orElse(null);
    }

    @Override
    public CompletableFuture<Suggestions> suggest(ExecutionContext<?> context, SuggestionsBuilder builder) {
        Arrays.stream(Planet.values())
                .map(Planet::getName)
                .forEach(builder::suggest);
        if (this.suggestionContextCallback != null) {
            this.suggestionContextCallback.accept(context);
        }
        return builder.buildFuture();
    }
}
