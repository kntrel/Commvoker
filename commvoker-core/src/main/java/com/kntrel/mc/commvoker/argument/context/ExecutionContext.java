package com.kntrel.mc.commvoker.argument.context;

import com.kntrel.mc.commvoker.argument.descriptor.InstancedArgumentDescriptor;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import java.util.Map;

public class ExecutionContext<S> extends ParameterContext {

    //FACTORY
    public static <S> ExecutionContext<S> copyOf(ExecutionContext<S> original, Map<String, Object> components) {
        return new ExecutionContext<>(original, original.commandContext(), components, original.previousArgumentDescriptors(), original.bag_);
    }
    public static <S> ExecutionContext<S> copyOf(ExecutionContext<S> original, List<InstancedArgumentDescriptor<S, ?>> previous) {
        return new ExecutionContext<>(original, original.commandContext(), original.components_, previous, original.bag_);
    }



    //FIELDS
    private final CommandContext<? extends S> commandContext_;
    private final Map<String, Object> components_;
    private final List<InstancedArgumentDescriptor<S, ?>> previous_;
    private final Map<String, Object> bag_;
    
    
    //CONSTRUCTOR
    public ExecutionContext(
            ParameterContext context,
            CommandContext<? extends S> commandContext,
            Map<String, Object> components,
            List<InstancedArgumentDescriptor<S, ?>> previous,
            Map<String, Object> bag
    ) {
        super(context.commandHolder(), context.parameter(), context.type(), context.method(), context.parameterIndex());
        this.commandContext_ = commandContext;
        this.components_ = Map.copyOf(components);
        this.previous_ = List.copyOf(previous);
        this.bag_ = bag;
    }
    
    
    //GETTERS
    public CommandContext<? extends S> commandContext() { return this.commandContext_; }
    public Map<String, Object> bag() { return this.bag_; }
    
    
    //COMPONENTS
    public <T> T component(String key, Class<T> type) {
        return fetchFromMap(this.components_, key, type, "The '" + key + "' component");
    }
    public Object component(String key) {
        return this.components_.get(key);
    }
    public boolean hasComponent(String key) {
        return this.components_.containsKey(key);
    }


    //BAG
    public <T> T bagObject(String key, Class<T> type) {
        return fetchFromMap(this.bag_, key, type, "The '" + key + "' bag object");
    }
    public Object bagObject(String key) {
        return this.bag_.get(key);
    }
    public boolean hasBagObject(String key) {
        return this.bag_.containsKey(key);
    }
    public void putBagObject(String key, Object value) {
        this.bag_.put(key, value);
    }


    //PREVIOUS
    public boolean hasPreviousArguments() {
        return !this.previous_.isEmpty();
    }
    public int previousArgumentsCount() {
        return this.previous_.size();
    }
    public Object previousArgument() {
        if (this.previous_.isEmpty()) { return null; }
        return this.previous_.getLast().value();
    }
    public Object previousArgument(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Previous item index must be >= 0. Provided: " + index);
        }
        if (this.previous_.isEmpty()) { return null; }
        index = this.previous_.size() - 1 - index;
        if (index < 0) { index = 0; }
        return this.previous_.get(index).value();
    }
    public <T> T previousArgument(int index, Class<T> type) {
        Object o = this.previousArgument(index);
        if (o == null) { return null; }
        if (!type.isAssignableFrom(o.getClass())) {
            throw new ClassCastException("The previous argument at index " + index + " is of type '" + o.getClass().getName() + "'. Not compatible with '" + type.getName() + "'");
        }
        return type.cast(o);
    }
    public <T> T previousArgumentOfType(Class<T> type) {
        for (int i = this.previousArgumentsCount() - 1; i >= 0; i--) {
            Object o = this.previousArgument(i);
            if (type.isAssignableFrom(o.getClass())) {
                return type.cast(o);
            }
        }
        return null;
    }
    public List<Object> previousArguments() {
        return this.previous_.stream()
                .map(d -> (Object) d.value())
                .toList();
    }
    public List<InstancedArgumentDescriptor<S, ?>> previousArgumentDescriptors() {
        return this.previous_;
    }


    //COMMAND CONTEXT
    public <T> T commandArgument(String name, Class<T> type) {
        return this.commandContext_.getArgument(name, type);
    }
    public S source() { return this.commandContext_.getSource(); }


    //PRIVATE
    private static  <T> T fetchFromMap(Map<String, Object> map, String key, Class<T> type, String errorPrefix) {
        Object o = map.get(key);
        if (o == null) { return null; }
        if (!type.isAssignableFrom(o.getClass())) {
            throw new ClassCastException(errorPrefix + " is of type '" + o.getClass().getName() + "'. Not compatible with '" + type.getName() + "'");
        }
        return type.cast(o);
    }
}
