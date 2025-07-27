package com.kntrel.mc.commvoker.bukkit.provided;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class BukkitUndefinedBindings {

    static {
        EntityArgument arg = EntityArgument.player();

        EntitySelector selector;
        try {
            selector = arg.parse(new StringReader(""));
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
        List<ServerPlayer> players;
        try {
            players = selector.findPlayers(null);
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
        players.get(0).getBukkitEntity();
    }

}
