package com.kntrel.commvoker.spigot;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import java.lang.reflect.InvocationTargetException;

class BukkitAccessors {

    private BukkitAccessors() {}


    static CommandDispatcher<CommandSourceStack> commandDispatcher(Server server) {
        MinecraftServer ms;
        try {
            ms = (MinecraftServer) server.getClass().getMethod("getServer").invoke(server);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return ms.getCommands().getDispatcher();
    }

    static CommandMap commandMap(Server server) {
        try {
            return (CommandMap) server.getClass().getMethod("getCommandMap").invoke(server);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}