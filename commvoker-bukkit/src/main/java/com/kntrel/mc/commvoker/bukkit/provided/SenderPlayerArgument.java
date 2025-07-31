package com.kntrel.mc.commvoker.bukkit.provided;

import com.kntrel.mc.commvoker.argument.type.ImplicitArgumentType;
import com.mojang.brigadier.context.CommandContext;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SenderPlayerArgument implements ImplicitArgumentType<CommandSender, Player> {

    private static final SenderPlayerArgument INSTANCE = new SenderPlayerArgument();


    public static SenderPlayerArgument senderPlayer() { return INSTANCE; }


    private SenderPlayerArgument() {}

    @Override
    public Player parse(CommandContext<CommandSender> ctx) {
        return (ctx.getSource() instanceof Player p) ? p : null;
    }
}
