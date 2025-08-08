package com.kntrel.mc.commvoker.bukkit.internal;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CraftBukkitAccessors {

    private CraftBukkitAccessors() {}

    private static final Method GET_LISTENER;

    static {

        try {
            String obc = Bukkit.getServer().getClass().getPackage().getName();
            Class<?> wrapper = Class.forName(obc + ".command.VanillaCommandWrapper");
            GET_LISTENER = wrapper.getMethod("getListener", CommandSender.class);
        } catch (ReflectiveOperationException ex) {
            throw new ExceptionInInitializerError(ex);
        }

    }


    public static Object getCommandSourceStack(CommandSender commandSender) {
        try {
            return GET_LISTENER.invoke(null, commandSender);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
