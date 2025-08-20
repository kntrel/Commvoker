package com.kntrel.mc.commvoker.argument.binding;

import java.util.Map;

public class Components {

    //FIELDS
    private final Map<String, Object> map_;


    //CONSTRUCTOR
    public Components(Map<String, Object> map) {
        this.map_ = map;
    }


    //UTILITY
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object o = this.map_.get(key);
        if (o == null) { return null; }
        if (!type.isAssignableFrom(o.getClass())) {
            throw new ClassCastException("The '" + key + "' component is of type '" + o.getClass().getName() + "'. Not compatible with '" + type.getName() + "'");
        }
        return (T) o;
    }
    public Object get(String key) {
        return this.map_.get(key);
    }
    public Components merge(Components other) {
        for (String k : other.map_.keySet()) if (this.map_.containsKey(k)) {
            throw new IllegalArgumentException("Key '" + k + "' is already present in the target Components map.");
        }
        this.map_.putAll(other.map_);
        return this;
    }
    public boolean has(String key) {
        return this.map_.containsKey(key);
    }
}
