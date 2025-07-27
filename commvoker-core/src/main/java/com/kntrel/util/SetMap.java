package com.kntrel.util;

import java.util.*;
import java.util.function.Supplier;

public class SetMap<K, V> implements Map<K, Set<V>> {

    //CONSTANTS
    private static <V> Supplier<? extends Set<V>> defaultSupplier() {
        return HashSet::new;
    }

    //FIELDS
    private final Map<K, Set<V>> delegate;
    private final Supplier<? extends Set<V>> setSupplier_;


    //CONSTRUCTORS
    public SetMap(Supplier<? extends Set<V>> setSupplier) {
        this.delegate = new HashMap<>();
        this.setSupplier_ = setSupplier;
    }
    public SetMap() {
        this(defaultSupplier());
    }


    @Override public int size() {
        return this.delegate.size();
    }

    @Override public boolean isEmpty() {
        return this.delegate.isEmpty();
    }

    public boolean isEmpty(K key) {
        return this.delegate.get(key).isEmpty();
    }

    @Override public boolean containsKey(Object key) {
        return this.delegate.containsKey(key);
    }

    @Override public boolean containsValue(Object value) {
        return this.flatValues().stream().anyMatch(v -> v.equals(value));
    }

    @Override public Set<V> get(Object key) {
        return this.delegate.get(key);
    }

    @Override public Set<V> put(K key, Set<V> value) {
        Set<V> set = this.delegate.computeIfAbsent(key, k -> this.setSupplier_.get());
        set.addAll(value);
        return set;
    }

    public Set<V> putInto(K key, V value) {
        Set<V> set = this.delegate.computeIfAbsent(key, k -> this.setSupplier_.get());
        set.add(value);
        return set;
    }

    @Override public Set<V> remove(Object key) {
        return this.delegate.remove(key);
    }

    @Override public void putAll(Map<? extends K, ? extends Set<V>> m) {
        this.delegate.putAll(m);
    }

    public void putAllInto(Map<? extends K, ? extends V> m) {
        m.forEach(this::putInto);
    }

    @Override public void clear() {
        this.delegate.clear();
    }

    @Override public Set<K> keySet() {
        return this.delegate.keySet();
    }

    @Override public Collection<Set<V>> values() {
        return this.delegate.values();
    }

    public Collection<V> flatValues() {
        return this.values().stream()
                .flatMap(Set::stream)
                .toList();
    }

    @Override public Set<Entry<K, Set<V>>> entrySet() {
        return this.delegate.entrySet();
    }
}
