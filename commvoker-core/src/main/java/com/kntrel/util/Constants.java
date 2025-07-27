package com.kntrel.util;

import java.lang.reflect.Modifier;
import java.util.*;

public final class Constants {

    private Constants() {}

    public static <T> Collection<T> getAll(Class<?> holder, Class<T> constantType) {
        return Arrays.stream(holder.getDeclaredFields())
                .filter(f ->
                           constantType.isAssignableFrom(f.getType())
                        && Modifier.isStatic(f.getModifiers())
                        && Modifier.isFinal(f.getModifiers())
                )
                .map(f -> {
                    f.setAccessible(true);
                    try {
                        return (T) f.get(null);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }
}
