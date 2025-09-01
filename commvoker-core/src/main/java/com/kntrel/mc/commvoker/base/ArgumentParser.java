package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.argument.binding.ArgumentDescriptor;
import com.kntrel.mc.commvoker.argument.binding.CommandTemplate;
import com.kntrel.mc.commvoker.argument.binding.Contextualizer;
import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.argument.descriptor.InstancedArgumentDescriptor;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

class ArgumentParser<S> {

    //ASSETS
    private static final Map<String, String> EMPTY = Collections.emptyMap();


    //FIELDS
    private final Map<String, String> namesMap_;
    private final ArgumentDescriptor<? super S, ?> descriptor_;


    //CONSTRUCTORS
    ArgumentParser(Map<String, String> namesMap, ArgumentDescriptor<? super S, ?> descriptor) {
        this.namesMap_ = namesMap;
        this.descriptor_ = descriptor;
    }
    ArgumentParser(Function<ExecutionContext<? extends S>, ?> implicitContextualizer) {
        this(EMPTY, new ArgumentDescriptor<>() {
            @Override public CommandTemplate<S> template() { return CommandTemplate.empty(); }
            @Override public Contextualizer<S, Object> contextualizer() { return implicitContextualizer::apply; }
        });
    }

    //GETTERS
    public ArgumentDescriptor<? super S, ?> argumentDescriptor() { return this.descriptor_; }

    //UTIL
    InstancedArgumentDescriptor<S, ?> parse(CommandContext<? extends S> ctx, List<InstancedArgumentDescriptor<S, ?>> previous, Map<String, Object> bag) {
        Map<String, Object> compMap = new HashMap<>();

        for (var e : this.namesMap_.entrySet()) try {
            Object o = ctx.getArgument(e.getKey(), Object.class);
            compMap.put(e.getValue(), o);
        } catch (IllegalArgumentException ignored) {}

        Object val = this.descriptor_.contextualizer().contextualize(new ExecutionContext<>(ctx, compMap, previous, bag));
        return InstancedArgumentDescriptor.of((ArgumentDescriptor<S, Object>) this.descriptor_, val);
    }

    boolean canParse(CommandContext<?> ctx) {
        if (this.namesMap_.isEmpty()) return true;

        for (String key : this.namesMap_.keySet()) {
            try {
                ctx.getArgument(key, Object.class);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        return true;
    }
}