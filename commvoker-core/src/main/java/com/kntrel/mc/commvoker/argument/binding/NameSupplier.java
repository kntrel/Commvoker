package com.kntrel.mc.commvoker.argument.binding;

import java.util.Map;

public interface NameSupplier {

    String supply(String key);
    Map<String, String> namesMap();
}
