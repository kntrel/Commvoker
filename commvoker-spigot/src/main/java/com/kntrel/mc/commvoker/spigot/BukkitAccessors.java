package com.kntrel.mc.commvoker.spigot;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.craftbukkit.CraftServer;

class BukkitAccessors {

    private BukkitAccessors() {}


    static MinecraftServer minecraftServer(Server server) {
        if (server instanceof CraftServer cs) {
            return cs.getServer();
        }
        throw new RuntimeException("Not a CraftBukkit server");
    }

    static CommandDispatcher<CommandSourceStack> commandDispatcher(Server server) {
        return minecraftServer(server).getCommands().getDispatcher();
    }

    static CommandMap commandMap(Server server) {
        if (server instanceof CraftServer cs) {
            return cs.getCommandMap();
        }
        throw new RuntimeException("Not a CraftBukkit server");
    }
}