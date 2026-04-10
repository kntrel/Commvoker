package com.kntrel.mc.commvoker.argument.binding;

import java.util.Map;

/**
 * Internal API. Public for framework wiring, but not part of the supported external API.
 */
public interface NameSupplier {

    String supply(String key);
    Map<String, String> namesMap();
}
