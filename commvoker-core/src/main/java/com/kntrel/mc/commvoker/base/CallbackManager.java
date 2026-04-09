package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.callback.ReturnCallback;
import com.kntrel.util.TypeUtils;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
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
        Class<?> cls = callback.getClass();

        Type found = resolveReturnCallback(cls, new HashMap<>());
        if (found != null) { return found; }

        throw new RuntimeException("Cannot resolve ReturnCallback generic type for " + cls.getName());
    }

    private static Type resolveReturnCallback(Type type, Map<TypeVariable<?>, Type> subst) {
        if (type instanceof Class<?> c) {
            // interfaces
            for (Type it : c.getGenericInterfaces()) {
                Type t = resolveReturnCallback(it, subst);
                if (t != null) { return t; }
            }
            // superclass
            Type sup = c.getGenericSuperclass();
            if (sup != null) { return resolveReturnCallback(sup, subst); }
            return null;
        }

        if (type instanceof ParameterizedType pt) {
            Type rawT = pt.getRawType();
            if (!(rawT instanceof Class<?> raw)) { return null; }

            // extend substitution map with this PT's args
            Map<TypeVariable<?>, Type> next = new HashMap<>(subst);
            TypeVariable<?>[] params = raw.getTypeParameters();
            Type[] actuals = pt.getActualTypeArguments();
            for (int i = 0; i < params.length && i < actuals.length; i++) {
                next.put(params[i], apply(next, actuals[i]));
            }

            // if this is ReturnCallback<S,T>, return T (after substitution)
            if (raw.equals(ReturnCallback.class)) {
                Type tArg = pt.getActualTypeArguments()[1];
                return apply(next, tArg);
            }

            // otherwise, keep walking: raw's super-interfaces and superclass
            for (Type it : raw.getGenericInterfaces()) {
                Type t = resolveReturnCallback(apply(next, it), next);
                if (t != null) return t;
            }
            Type sup = raw.getGenericSuperclass();
            if (sup != null) return resolveReturnCallback(apply(next, sup), next);

            return null;
        }

        return null;
    }

    // Reuse your TypeUtils.apply() if you want; included minimal version here:
    private static Type apply(Map<TypeVariable<?>, Type> map, Type t) {
        if (t instanceof TypeVariable<?> tv) {
            Type r = map.get(tv);
            return (r == null) ? tv : apply(map, r);
        }
        if (t instanceof ParameterizedType pt) {
            Type[] args = pt.getActualTypeArguments();
            Type[] na = new Type[args.length];
            for (int i = 0; i < args.length; i++) na[i] = apply(map, args[i]);
            return new TypeUtils.SimplePT((Class<?>) pt.getRawType(), na, pt.getOwnerType());
        }
        return t;
    }
}