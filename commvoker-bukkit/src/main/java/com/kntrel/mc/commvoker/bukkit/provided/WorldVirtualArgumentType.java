package com.kntrel.mc.commvoker.bukkit.provided;

import com.kntrel.mc.commvoker.argument.type.VirtualArgumentType;
import com.mojang.brigadier.context.CommandContext;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WorldVirtualArgumentType implements VirtualArgumentType<CommandSender, World> {

    private static final WorldVirtualArgumentType INSTANCE = new WorldVirtualArgumentType();

    public static WorldVirtualArgumentType senderWorld() {
        return INSTANCE;
    }


    private WorldVirtualArgumentType() {}

    @Override
    public World parse(CommandContext<CommandSender> ctx) {
        return (ctx.getSource() instanceof Player p) ? p.getWorld() : null;
    }
}
