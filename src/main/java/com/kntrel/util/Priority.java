package com.kntrel.util;

public class Priority implements Comparable<Priority> {

    //HANDY CONSTANTS
    public static final Priority
        HIGHEST = Priority.of(Integer.MAX_VALUE - 100),
        HIGHER = Priority.of(Integer.MAX_VALUE / 3 * 2),
        HIGH = Priority.of(Integer.MAX_VALUE / 3),
        NORMAL = Priority.of(0),
        LOW = Priority.of(Integer.MIN_VALUE / 3),
        LOWER = Priority.of(Integer.MIN_VALUE / 3 * 2),
        LOWEST = Priority.of(Integer.MIN_VALUE + 100);


    //STATIC OPERATIONS
    public static Priority of(int value) {
        return new Priority(value);
    }
    public static Priority above(Priority other) {
        return new Priority(other.val_ + 1);
    }
    public static Priority bellow(Priority other) {
        return new Priority(other.val_ - 1);
    }
    public static Priority min(Priority first, Priority... rest) {
        if (rest.length < 1) { return first; }
        Priority min = first;
        for (Priority p : rest) {
            if (p.lowerThan(min)) { min = p; }
        }
        return min;
    }
    public static Priority max(Priority first, Priority... rest) {
        if (rest.length < 1) { return first; }
        Priority max = first;
        for (Priority p : rest) {
            if (p.higherThan(max)) { max = p; }
        }
        return max;
    }
    public static Priority between(Priority a, Priority b) {
        long val = (((long) a.val_ + (long) b.val_)) >> 1;
        return new Priority((int) val);
    }


    //FIELDS
    private final int val_;


    //CONSTRUCTORS
    private Priority(int priority) {
        this.val_ = priority;
    }


    //GETTERS
    public int toInt() {
        return this.val_;
    }


    //UTIL
    @Override public int compareTo(Priority o) {
        return Integer.compare(this.val_, o.val_);
    }
    @Override public boolean equals(Object o) {
        if (o == null) { return false; }
        if (o == this) { return true; }
        if (o instanceof Number n) {
            return n.intValue() == this.val_;
        }
        if (o instanceof Priority p) {
            return this.val_ == p.val_;
        }
        return false;
    }
    public boolean higherThan(Priority o) {
        return this.val_ > o.val_;
    }
    public boolean lowerThan(Priority o) {
        return this.val_ < o.val_;
    }
    public boolean higherEqualsThan(Priority o) {
        return this.val_ >= o.val_;
    }
    public boolean lowerEqualsThan(Priority o) {
        return this.val_ <= o.val_;
    }
    @Override public int hashCode() {
        return Integer.hashCode(this.val_);
    }
}
