package com.kntrel.mc.commvoker.bukkit.provided;

import com.kntrel.mc.commvoker.argument.type.VirtualArgumentType;
import com.mojang.brigadier.context.CommandContext;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerVirtualArgumentType implements VirtualArgumentType<CommandSender, Player> {

    private static final PlayerVirtualArgumentType INSTANCE = new PlayerVirtualArgumentType();


    public static PlayerVirtualArgumentType senderPlayer() { return INSTANCE; }


    private PlayerVirtualArgumentType() {}

    @Override
    public Player parse(CommandContext<CommandSender> ctx) {
        return (ctx.getSource() instanceof Player p) ? p : null;
    }
}
