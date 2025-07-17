package com.kntrel.mc.commvoker.base;


import com.kntrel.mc.commvoker.argument.ArgumentTypeRegistry;
import com.kntrel.mc.commvoker.argument.VirtualArgumentType;
import com.mojang.brigadier.arguments.ArgumentType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;

public final class ArgumentTypeResolver<S> implements ArgumentTypeRegistry<S> {

    public record Result<S, T>(VirtualArgumentType<S, T> virtual, ArgumentType<T> parsed) {
        public Result {
            if (virtual == null && parsed == null) {
                throw new IllegalArgumentException("Either the virtual or parsed argument type must not be null");
            }
        }
        public boolean isVirtual() { return this.virtual != null; }
        public boolean isParsed() { return !this.isVirtual(); }
    }

    private static final class Entry<S, T> {
        // “Default” types (no predicate)
        ArgumentType<T> defaultParsed;
        VirtualArgumentType<S, T> defaultVirtual;

        // Predicate‑conditioned types
        final Map<BiPredicate<Class<T>, Parameter>, ArgumentType<T>> parsedChecks   = new LinkedHashMap<>();
        final Map<BiPredicate<Class<T>, Parameter>, VirtualArgumentType<S, T>> virtualChecks  = new LinkedHashMap<>();
    }

    private final Map<Class<?>, Entry<S, ?>> entries = new HashMap<>();


    @SuppressWarnings("unchecked")
    public <T> Optional<Result<S, T>> resolve(Class<T> type, Parameter parameter) {

        if (!type.equals(parameter.getType())) {
            throw new IllegalArgumentException("Provided type '%s' doesn't match the parameter type '%s'".formatted(type.getName(), parameter.getType().getName()));
        }

        Entry<S, T> entry = (Entry<S, T>) entries.get(type);
        if (entry == null) { return Optional.empty(); }

        for (var e : entry.parsedChecks.entrySet()) {
            if (e.getKey().test(type, parameter)) {
                return Optional.of(new Result<>(null, e.getValue()));
            }
        }

        for (var e : entry.virtualChecks.entrySet()) {
            if (e.getKey().test(type, parameter)) {
                return Optional.of(new Result<>(e.getValue(), null));
            }
        }

        if (entry.defaultParsed != null) {
            return Optional.of(new Result<>(null, entry.defaultParsed));
        }
        if (entry.defaultVirtual != null) {
            return Optional.of(new Result<>(entry.defaultVirtual, null));
        }

        return Optional.empty();
    }


    @Override public <T> void register(Class<T> type, ArgumentType<T> arg) {
        register(type, null, arg);
    }
    @Override public <T> void register(Class<? extends Annotation> ann, Class<T> type, ArgumentType<T> arg) {
        register(type, (t,p) -> p.isAnnotationPresent(ann), arg);
    }
    @Override @SuppressWarnings("unchecked")
    public <T> void register(Class<T> type, BiPredicate<Class<T>, Parameter> check, ArgumentType<T> arg) {

        Entry<S, T> e = (Entry<S, T>) entries.computeIfAbsent(type, k -> new Entry<>());
        if (check == null) {
            if (e.defaultParsed != null)
                throw new IllegalStateException("Default ArgumentType already defined for '%s'".formatted(type.getName()));
            e.defaultParsed = arg;
        } else {
            e.parsedChecks.put(check, arg);
        }
    }
    @Override public <T> void register(Class<T> type, VirtualArgumentType<S, T> arg) {
        register(type, null, arg);
    }
    @Override public <T> void register(Class<? extends Annotation> ann, Class<T> type,
                                       VirtualArgumentType<S, T> arg) {
        register(type, (t,p) -> p.isAnnotationPresent(ann), arg);
    }
    @Override @SuppressWarnings("unchecked")
    public <T> void register(Class<T> type, BiPredicate<Class<T>, Parameter> check, VirtualArgumentType<S, T> arg) {
        Entry<S, T> e = (Entry<S, T>) entries.computeIfAbsent(type, k -> new Entry<>());
        if (check == null) {
            if (e.defaultVirtual != null)
                throw new IllegalStateException("Default VirtualArgumentType already defined for '%s'".formatted(type.getName()));
            e.defaultVirtual = arg;
        } else {
            e.virtualChecks.put(check, arg);
        }
    }
}