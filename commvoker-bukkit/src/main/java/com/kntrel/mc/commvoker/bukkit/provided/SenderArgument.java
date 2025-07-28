package com.kntrel.mc.commvoker.bukkit.provided;

import com.kntrel.mc.commvoker.argument.type.ImplicitArgumentType;
import com.mojang.brigadier.context.CommandContext;
import org.bukkit.command.CommandSender;

public class SenderArgument implements ImplicitArgumentType<CommandSender, CommandSender> {

    private static final SenderArgument INSTANCE = new SenderArgument();

    public static SenderArgument sender() {
        return INSTANCE;
    }



    private SenderArgument() {}

    @Override
    public CommandSender parse(CommandContext<CommandSender> ctx) {
        return ctx.getSource();
    }
}
