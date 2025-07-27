package com.kntrel.util;

import java.util.Collection;
import java.util.function.Predicate;

public class Multipredicate<T> implements Predicate<T> {

    @SafeVarargs
    public static <T> Multipredicate<T> or(Predicate<T>... tests) {
        if (tests.length < 1) {
            return null;
        }
        return new Multipredicate<>(Mode.OR, tests);
    }

    public static <T> Multipredicate<T> and(Predicate<T>... tests) {
        if (tests.length < 1) {
            return null;
        }
        return new Multipredicate<>(Mode.AND, tests);
    }

    @SuppressWarnings("unchecked")
    public static <T> Multipredicate<T> or(Collection<Predicate<T>> tests) {
        if (tests.isEmpty()) {
            return null;
        }
        return new Multipredicate<>(Mode.OR, tests.toArray(new Predicate[0]));
    }

    @SuppressWarnings("unchecked")
    public static <T> Multipredicate<T> and(Collection<Predicate<T>> tests) {
        if (tests.isEmpty()) {
            return null;
        }
        return new Multipredicate<>(Mode.AND, tests.toArray(new Predicate[0]));
    }



    private enum Mode { AND, OR }


    private final Mode mode_;
    private final Predicate<T>[] tests_;

    private Multipredicate(Mode mode, Predicate<T>[] tests) {
        this.tests_ = tests;
        this.mode_ = mode;
    }

    @Override public boolean test(T t) {
        if (this.mode_.equals(Mode.OR)) {
            for (Predicate<T> p : this.tests_) {
                if (p.test(t)) { return true; }
            }
            return false;
        }
        for (Predicate<T> p : this.tests_) {
            if (!p.test(t)) { return false; }
        }
        return true;
    }
}
