package com.kntrel.mc.commvoker.assembler;

class Utils {

    private Utils() {}

    static boolean hasMethod(Class<?> type, String name, Class<?>... paramTypes) {
        try {
            type.getDeclaredMethod(name, paramTypes);
        } catch (NoSuchMethodException e) {
            return false;
        }
        return true;
    }

}
