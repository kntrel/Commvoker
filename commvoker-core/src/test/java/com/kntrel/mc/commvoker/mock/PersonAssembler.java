package com.kntrel.mc.commvoker.mock;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.BiComposedAssembler;
import com.kntrel.mc.commvoker.provided.assemblers.IntegerAssembler;
import com.kntrel.mc.commvoker.provided.assemblers.StringAssembler;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class PersonAssembler implements BiComposedAssembler<Object, String, Integer, Person> {

    //FIELDS
    private final Consumer<ExecutionContext<?>> contextConsumer_;


    //CONSTRUCTOR
    public PersonAssembler(Consumer<ExecutionContext<?>> contextConsumer) {
        this.contextConsumer_ = contextConsumer;
    }
    public PersonAssembler() {
        this(null);
    }


    //IMPLEMENTATION
    @Override
    public Assembler<? super Object, ? extends String> firstDelegate() {
        return StringAssembler.string();
    }

    @Override
    public Assembler<? super Object, ? extends Integer> secondDelegate() {
        return IntegerAssembler.integer(0, 120);
    }

    @Override
    public Person compose(ExecutionContext<?> ctx, String first, Integer second) {
        return new Person(first, second);
    }

    @Override
    public CompletableFuture<Suggestions> secondSuggest(ExecutionContext<?> ctx, String name, SuggestionsBuilder suggestionsBuilder) {
        if (this.contextConsumer_ != null) {
            this.contextConsumer_.accept(ctx);
        }
        return suggestionsBuilder.buildFuture();
    }
}
