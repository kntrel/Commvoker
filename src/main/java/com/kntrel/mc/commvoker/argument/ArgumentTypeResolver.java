package com.kntrel.mc.commvoker.argument;


import com.mojang.brigadier.arguments.ArgumentType;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;

public class ArgumentTypeResolver implements ArgumentTypeRegistry {

    private static class Entry<T> {
        ArgumentType<T> unchecked;
        Map<BiPredicate<Class<T>, Parameter>, ArgumentType<T>> checks;
    }


    private final Map<Class<?>, Entry<?>> entries_;


    public ArgumentTypeResolver() {
        this.entries_ = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<ArgumentType<?>> resolve(Class<T> type, Parameter parameter) {
        if (!type.equals(parameter.getType())) {
            throw new IllegalArgumentException("Provided type '" + type.getName() + "' doesn't match the parameter type '" + parameter.getType().getName() + "'");
        }

        if (!this.entries_.containsKey(type)) {
            return Optional.empty();
        }

        Entry<T> entry = (Entry<T>) this.entries_.get(type);
        ArgumentType<?> result = entry.checks.entrySet().stream()
                .filter(e -> e.getKey().test(type, parameter))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);

        if (result != null) { return Optional.of(result); }
        return Optional.ofNullable(entry.unchecked);
    }

    @Override
    public <T> void register(Class<T> type, ArgumentType<T> argumentType) {
        this.register(type, null, argumentType);
    }
    @Override
    public <T> void register(Class<? extends Annotation> annotation, Class<T> type, ArgumentType<T> argumentType) {
        this.register(type, (t, p) -> p.isAnnotationPresent(annotation), argumentType);
    }
    @Override @SuppressWarnings("unchecked")
    public <T> void register(Class<T> type, @Nullable BiPredicate<Class<T>, Parameter> check, ArgumentType<T> argumentType) {
        Entry<T> entry = (Entry<T>) this.entries_.computeIfAbsent(type, t -> new Entry<T>());

        if (check != null) {
            entry.checks.put(check, argumentType);
        }

        if (entry.unchecked == null) {
            throw new IllegalStateException("An ArgumentType entry for '" + type.getName() + "' with no checks is already defined.");
        }

        entry.unchecked = argumentType;
    }

}
