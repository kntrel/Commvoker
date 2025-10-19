package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.argument.descriptor.InstancedArgumentDescriptor;
import com.kntrel.mc.commvoker.callback.ReturnCallback;
import com.kntrel.mc.commvoker.command.CommandMethod;
import com.kntrel.mc.commvoker.command.CommandMethodArgument;
import com.kntrel.mc.commvoker.command.CommandMethodContext;
import com.kntrel.mc.commvoker.error.CommandExceptionResolver;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

class CommandMethodInvoker<S> implements Command<S> {

    //FIELDS
    private final CommandMethod method_;
    private final ArgumentParser<S>[] argumentParsers_;
    private final Object commandHolder_;
    private final CommandExceptionResolver exceptionResolver_;
    private final List<ReturnCallback<? super S, ?>> returnCallbacks_;
    private Predicate<S> requirement_;



    //CONSTRUCTORS
    CommandMethodInvoker(Object instance, CommandMethod method, ArgumentParser<S>[] arguments, CommandExceptionResolver exceptionResolver) {
        if (method.getParameterCount() != arguments.length) {
            throw new IllegalArgumentException("Method argument count isn't equal to ArgumentParser array length");
        }

        this.commandHolder_ = instance;
        this.method_ = method;
        this.argumentParsers_ = arguments;
        this.exceptionResolver_ = exceptionResolver;
        this.returnCallbacks_ = new ArrayList<>();
        this.requirement_ = null;
    }


    //IMPLEMENTATION
    @Override public int run(CommandContext<S> ctx) throws CommandSyntaxException {
        if (this.requirement_ != null && !this.requirement_.test(ctx.getSource())) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().create();
        }

        try {
            return this.runInner(ctx);
        } catch (CommandSyntaxException e) {
            throw e;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (Throwable e) {
            Throwable throwable = e;
            if (throwable instanceof InvocationTargetException ie) { throwable = ie.getTargetException(); }
            CommandSyntaxException resolved = this.exceptionResolver_.resolve(throwable);
            if (resolved != null) {
                throw resolved;
            }
            throw new RuntimeException(throwable);
        }
    }


    //GETTERS
    public CommandMethod getCommandMethod() {
        return this.method_;
    }
    public Method getMethod() {
        return this.method_.getMethod();
    }
    public Class<?> getReturnType() {
        return this.method_.getReturnType();
    }
    public Type getGenericReturnType() {
        return this.method_.getGenericReturnType();
    }


    //SETTERS
    public CommandMethodInvoker<S> requires(Predicate<S> requirement) {
        this.requirement_ = (this.requirement_ == null)
                ? requirement
                : this.requirement_.and(requirement);
        return this;
    }
    public void addCallback(ReturnCallback<? super S, ?> callback) {
        this.returnCallbacks_.add(callback);
    }
    @SuppressWarnings("rawtypes")
    public void removeCallback(Class<? extends ReturnCallback> callbackClass) {
        this.returnCallbacks_.removeIf(cb -> cb.getClass().equals(callbackClass));
    }


    //PRIVATE
    public int runInner(CommandContext<S> ctx) throws InvocationTargetException, IllegalAccessException, CommandSyntaxException {
        int len = this.argumentParsers_.length;
        List<InstancedArgumentDescriptor<S, ?>> descriptors = new ArrayList<>(len);
        Object[] args = new Object[len];
        List<CommandMethodArgument> methodArgs = new ArrayList<>(len);
        Map<String, Object> bag = new HashMap<>();
        int commandIndex = 0;

        for (int i = 0; i < len; i++) {
            ArgumentParser<S> parser = this.argumentParsers_[i];
            InstancedArgumentDescriptor<S, ?> desc = parser.parse(ctx, descriptors, bag);
            descriptors.add(desc);
            args[i] = desc.value();
            CommandMethodArgument methodArg = parser.isImplicit()
                    ? new CommandMethodArgument.Implicit(i, desc.value())
                    : new CommandMethodArgument.Explicit(i, desc.value(), this.method_.getTokenAt(commandIndex++).label());
            methodArgs.add(methodArg);
        }

        Object returned = this.method_.getMethod().invoke(this.commandHolder_, args);

        if (!this.returnCallbacks_.isEmpty()) {
            CommandMethodContext<S> methodContext = new CommandMethodContext<>(this.method_, ctx, methodArgs, bag);
            for (ReturnCallback<? super S, ?> callback : this.returnCallbacks_) {
                ((ReturnCallback<S, Object>) callback).onReturn(methodContext, returned);
            }
        }

        if (Number.class.isAssignableFrom(this.method_.getReturnType())) {
            return (int) returned;
        }

        return 0;
    }
}
