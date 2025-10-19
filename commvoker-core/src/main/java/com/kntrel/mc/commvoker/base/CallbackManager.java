package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.callback.ReturnCallback;
import com.kntrel.mc.commvoker.command.CommandMethodContext;
import com.kntrel.util.TypeUtils;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

class CallbackManager<S> {

    //FIELDS
    private final Map<Class<?>, List<CommandMethodInvoker<S>>> invokers_;
    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends ReturnCallback>, ReturnCallback<? super S, ?>> callbacks_;
    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends ReturnCallback>, List<CommandMethodInvoker<S>>> callbackMap_;


    //CONSTRUCTORS
    CallbackManager() {
        this.invokers_ = new HashMap<>();
        this.callbacks_ = new HashMap<>();
        this.callbackMap_ = new HashMap<>();
    }


    //UTILITY
    void addInvoker(CommandMethodInvoker<S> invoker) {
        Class<?> returnType = TypeUtils.boxed(invoker.getReturnType());
        this.invokers_
            .computeIfAbsent(returnType, k -> new ArrayList<>())
            .add(invoker);

        for (ReturnCallback<? super S, ?> callback : this.callbacks_.values()) {
            Type callbackType = getCallbackType(callback);
            Type invokerType = invoker.getGenericReturnType();
            if (TypeUtils.isSubtypeOf(invokerType, callbackType) && callback.listensTo(invoker.getCommandMethod())) {
                invoker.addCallback(callback);
                this.callbackMap_.computeIfAbsent(callback.getClass(), k -> new ArrayList<>()).add(invoker);
            }
        }
    }

    void addCallback(ReturnCallback<? super S, ?> callback) {
        if (this.callbacks_.containsKey(callback.getClass())) {
            throw new RuntimeException("Callback of type " + callback.getClass().getName() + " is already registered");
        }
        this.callbacks_.put(callback.getClass(), callback);

        Type callbackType = getCallbackType(callback);
        Class<?> callbackClass;
        if (callbackType instanceof Class<?> cls) {
            callbackClass = cls;
        } else if (callbackType instanceof ParameterizedType pType) {
            callbackClass = (Class<?>) pType.getRawType();
        } else {
            throw new RuntimeException("Unsupported callback type: " + callbackType.getTypeName());
        }
        callbackClass = TypeUtils.boxed(callbackClass);

        List<CommandMethodInvoker<S>> invokers = this.invokers_.get(callbackClass);
        if (invokers == null) { return; }
        for (CommandMethodInvoker<S> invoker : invokers) {
            Type invokerType = invoker.getGenericReturnType();
            if (TypeUtils.isSubtypeOf(invokerType, callbackType) && callback.listensTo(invoker.getCommandMethod())) {
                invoker.addCallback(callback);
                this.callbackMap_.computeIfAbsent(callback.getClass(), k -> new ArrayList<>()).add(invoker);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    void removeCallback(Class<? extends ReturnCallback> callbackClass) {
        this.callbacks_.remove(callbackClass);
        List<CommandMethodInvoker<S>> invokers = this.callbackMap_.remove(callbackClass);
        if (invokers == null) { return; }
        invokers.forEach(invoker -> invoker.removeCallback(callbackClass));
    }


    //HELPERS
    private static Type getCallbackType(ReturnCallback<?, ?> callback) {
        Method method = Arrays.stream(callback.getClass().getMethods())
                .filter(m -> m.getName().equals("onReturn"))
                .filter(m -> m.getParameterCount() == 2)
                .filter(m -> m.getParameterTypes()[0].equals(CommandMethodContext.class))
                .findFirst().orElse(null);

        if (method == null) {
            throw new RuntimeException("No suitable onReturn method found in callback class " + callback.getClass().getName());
        }

        return method.getGenericParameterTypes()[1];
    }
}