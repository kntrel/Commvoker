package com.kntrel.mc.commvoker.base;

import java.lang.reflect.Array;
import java.util.Arrays;

class Utils {

    private Utils() {}

    static String toSnakeCase(String src) {
        if (src == null || src.isEmpty()) {
            return "";
        }

        StringBuilder out = new StringBuilder();
        out.append(Character.toLowerCase(src.charAt(0)));

        for (int i = 1; i < src.length(); i++) {
            char c = src.charAt(i);
            if (c == ' ' || Character.isUpperCase(c)) {
                out.append("_");
            }
            if (c == ' ') { continue; }
            out.append(Character.toLowerCase(c));
        }

        return out.toString();
    }

    @SafeVarargs
    static <T> T[] arrayJoin(T[] first, T[]... rest) {
        int len = first.length;
        for (T[] arr : rest) { len += arr.length; }

        T[] out = Arrays.copyOf(first, len);

        len = first.length;
        for (T[] arr : rest) {
            System.arraycopy(arr, 0, out, len, arr.length);
            len += arr.length;
        }

        return out;
    }

}
