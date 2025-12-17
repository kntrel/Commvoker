package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.argument.Component;
import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.argument.context.ParameterContext;
import com.kntrel.mc.commvoker.argument.descriptor.ArgumentDescriptor;
import com.kntrel.mc.commvoker.argument.descriptor.InstancedArgumentDescriptor;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ArgumentParser<S> {

    //ASSETS
    private static final Map<String, String> EMPTY = Collections.emptyMap();


    //FIELDS
    private final Map<String, String> namesMap_;
    private final ArgumentDescriptor<? super S, ?> descriptor_;
    private final ParameterContext parameterContext_;
    boolean implicit = false;


    //CONSTRUCTORS
    ArgumentParser(Map<String, String> namesMap, ArgumentDescriptor<? super S, ?> descriptor, ParameterContext argumentContext) {
        this.namesMap_ = namesMap;
        this.descriptor_ = descriptor;
        this.parameterContext_ = argumentContext;
    }
    ArgumentParser(ArgumentDescriptor<? super S, ?> descriptor, ParameterContext argumentContext) {
        this(EMPTY, descriptor, argumentContext);
        this.implicit = true;
    }

    //GETTERS
    public ArgumentDescriptor<? super S, ?> argumentDescriptor() { return this.descriptor_; }
    public boolean isImplicit() { return this.implicit; }

    //UTIL
    InstancedArgumentDescriptor<S, ?> parse(CommandContext<? extends S> ctx, List<InstancedArgumentDescriptor<S, ?>> previous, Map<String, Object> bag) {
        Object val = this.descriptor_.contextualizer().contextualize(new ExecutionContext<>(this.parameterContext_, ctx, this.components(ctx), previous, bag));
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

    Map<String, Component<S>> components(CommandContext<?> ctx) {
        Map<String, Component<S>> compMap = new HashMap<>();
        Map<String, ParsedCommandNode<S>> nodesMap = ctx.getNodes().stream()
                .collect(HashMap::new, (m, n) -> m.put(n.getNode().getName(), (ParsedCommandNode<S>) n), HashMap::putAll);

        for (var e : this.namesMap_.entrySet()) try {
            Object o = ctx.getArgument(e.getKey(), Object.class);
            ParsedCommandNode<S> node = nodesMap.get(e.getKey());
            Component<S> comp = Component.fromNodes(e.getKey(), o, List.of(node));
            compMap.put(e.getValue(), comp);
        } catch (IllegalArgumentException ignored) {}

        return compMap;
    }

    ParameterContext parameterContext() {
        return this.parameterContext_;
    }
}