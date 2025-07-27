package com.kntrel.mc.commvoker.bukkit.provided;

import com.kntrel.mc.commvoker.argument.type.VirtualArgumentType;
import com.mojang.brigadier.context.CommandContext;
import org.bukkit.command.CommandSender;

public class CommandSenderVirtualArgumentType implements VirtualArgumentType<CommandSender, CommandSender> {

    private static final CommandSenderVirtualArgumentType INSTANCE = new CommandSenderVirtualArgumentType();

    public static CommandSenderVirtualArgumentType sender() {
        return INSTANCE;
    }



    private CommandSenderVirtualArgumentType() {}

    @Override
    public CommandSender parse(CommandContext<CommandSender> ctx) {
        return ctx.getSource();
    }
}
