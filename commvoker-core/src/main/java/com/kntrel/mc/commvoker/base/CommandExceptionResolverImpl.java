package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.error.CommandExceptionHandler;
import com.kntrel.mc.commvoker.error.CommandExceptionResolver;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CommandExceptionResolverImpl implements CommandExceptionResolver {

    private record Resolution(Throwable exception, List<CommandExceptionHandler<?>> handlers) {}

    //FIELDS
    private final Map<Class<? extends Throwable>, List<CommandExceptionHandler<?>>> registry_;
    private final Map<Class<? extends CommandExceptionHandler<?>>, Class<? extends Throwable>> handleMap_;


    //CONSTRUCTORS
    public CommandExceptionResolverImpl() {
        this.registry_ = new HashMap<>();
        this.handleMap_ = new HashMap<>();

        this.registerHandler(new CommandExceptionHandler<CommandSyntaxException>() {
            @Override public Class<CommandSyntaxException> exceptionType() { return CommandSyntaxException.class; }
            @Override public CommandSyntaxException handle(CommandSyntaxException exception) { return exception; }
        });
    }


    //IMPLEMENTATION
    @Override @SuppressWarnings("unchecked")
    public void registerHandler(CommandExceptionHandler<?> handler) {
        Class<? extends Throwable> type = handler.exceptionType();
        this.registry_.computeIfAbsent(type, k -> new ArrayList<>()).add(handler);
        this.handleMap_.put((Class<? extends CommandExceptionHandler<?>>) handler.getClass(), type);
    }
    @Override
    public void unregisterHandler(Class<? extends CommandExceptionHandler<? extends Throwable>> handlerClass) {
        Class<? extends Throwable> type = this.handleMap_.get(handlerClass);
        if (type == null) { return; }
        List<CommandExceptionHandler<?>> handlers = this.registry_.get(type);
        if (handlers == null) { return; }
        handlers.removeIf(h -> h.getClass().equals(handlerClass));
    }
    @Override
    public void unregisterHandler(CommandExceptionHandler<? extends Throwable> handler) {
        Class<? extends Throwable> type = handler.exceptionType();
        List<CommandExceptionHandler<?>> handlers = this.registry_.get(type);
        if (handlers == null) { return; }
        handlers.remove(handler);
    }
    @Override @SuppressWarnings({ "unchecked", "rawtypes" })
    public CommandSyntaxException resolve(Throwable exception) {
        Resolution resolution = this.deepResolve(exception);
        if (resolution == null) { return null; }

        for (CommandExceptionHandler handler : resolution.handlers()) {
            if (handler.handles(resolution.exception())) {
                return handler.handle(resolution.exception());
            }
        }

        return null;
    }


    //PRIVATE HELPERS
    private Resolution deepResolve(Throwable exception) {
        while (exception != null) {
            List<CommandExceptionHandler<?>> found = this.typeResolve(exception.getClass());
            if (found != null) {
                return new Resolution(exception, found);
            }
            exception = exception.getCause();
        }
        return null;
    }
    private List<CommandExceptionHandler<?>> typeResolve(Class<? extends Throwable> type) {
        List<CommandExceptionHandler<?>> handlers = this.registry_.get(type);
        if (handlers != null) {
            return handlers;
        }
        for (var entry : this.registry_.entrySet()) {
            if (entry.getKey().isAssignableFrom(type)) {
                this.registry_.put(type, new ArrayList<>(entry.getValue()));
                return entry.getValue();
            }
        }
        return null;
    }
}
