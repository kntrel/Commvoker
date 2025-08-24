package com.kntrel.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class Multipredicate<T> implements Predicate<T> {

    @SafeVarargs
    public static <T> Multipredicate<T> or(Predicate<T>... tests) {
        if (tests.length < 1) {
            return null;
        }
        return new Multipredicate<>(Mode.OR, Arrays.asList(tests));
    }
    @SafeVarargs
    public static <T> Multipredicate<T> and(Predicate<T>... tests) {
        if (tests.length < 1) {
            return null;
        }
        return new Multipredicate<>(Mode.AND, Arrays.asList(tests));
    }
    public static <T> Multipredicate<T> or(Collection<Predicate<T>> tests) {
        if (tests.isEmpty()) {
            return null;
        }
        return new Multipredicate<>(Mode.OR, tests);
    }
    public static <T> Multipredicate<T> and(Collection<Predicate<T>> tests) {
        if (tests.isEmpty()) {
            return null;
        }
        return new Multipredicate<>(Mode.AND, tests);
    }



    private enum Mode { AND, OR }


    private final Mode mode_;
    private final List<Predicate<? super T>> tests_;

    private Multipredicate(Mode mode, Collection<Predicate<T>> tests) {
        this.mode_ = mode;
        this.tests_ = new ArrayList<>(tests.size());
        this.tests_.addAll(tests);
    }

    @Override public boolean test(T t) {
        if (this.mode_.equals(Mode.OR)) {
            for (Predicate<? super T> p : this.tests_) {
                if (p.test(t)) { return true; }
            }
            return false;
        }
        for (Predicate<? super T> p : this.tests_) {
            if (!p.test(t)) { return false; }
        }
        return true;
    }

    @Override
    public Predicate<T> and(Predicate<? super T> other) {
        if (this.mode_.equals(Mode.AND)) {
            this.tests_.add(other);
            return this;
        }
        return Predicate.super.and(other);
    }

    @Override
    public Predicate<T> or(Predicate<? super T> other) {
        if (this.mode_.equals(Mode.OR)) {
            this.tests_.add(other);
            return this;
        }
        return Predicate.super.or(other);
    }
}
