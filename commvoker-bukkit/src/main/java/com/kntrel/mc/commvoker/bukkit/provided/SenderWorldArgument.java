package com.kntrel.mc.commvoker.bukkit.provided;

import com.kntrel.mc.commvoker.argument.type.ImplicitArgumentType;
import com.mojang.brigadier.context.CommandContext;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SenderWorldArgument implements ImplicitArgumentType<CommandSender, World> {

    private static final SenderWorldArgument INSTANCE = new SenderWorldArgument();

    public static SenderWorldArgument senderWorld() {
        return INSTANCE;
    }


    private SenderWorldArgument() {}

    @Override
    public World parse(CommandContext<CommandSender> ctx) {
        return (ctx.getSource() instanceof Player p) ? p.getWorld() : null;
    }
}
