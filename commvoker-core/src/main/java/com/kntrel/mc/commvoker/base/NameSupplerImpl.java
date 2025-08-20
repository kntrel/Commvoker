package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.argument.binding.NameSupplier;
import java.util.HashMap;
import java.util.Map;

class NameSupplerImpl implements NameSupplier {

    //FIELDS
    private final Map<String, String> namesMap_;
    private final String[] names_;
    int cursor_;


    //CONSTRUCTORS
    NameSupplerImpl(String base) {
        this.namesMap_ = new HashMap<>();
        this.names_ = base.split("\\s+");
        this.cursor_ = 0;
    }


    //IMPLEMENTATION
    @Override public String supply(String key) {
        String out;
        if (this.cursor_ < this.names_.length) {
            out = this.names_[this.cursor_++];
        } else {
            out = this.names_[this.names_.length - 1] + (++this.cursor_ - this.names_.length);
        }
        this.namesMap_.put(out, key);
        return out;
    }

    @Override public Map<String, String> namesMap() {
        return this.namesMap_;
    }
}
