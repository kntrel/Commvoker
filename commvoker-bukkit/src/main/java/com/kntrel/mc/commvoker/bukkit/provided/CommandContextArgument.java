package com.kntrel.mc.commvoker.bukkit.provided;

import com.kntrel.mc.commvoker.argument.type.ImplicitArgumentType;
import com.mojang.brigadier.context.CommandContext;
import org.bukkit.command.CommandSender;

public class CommandContextArgument implements ImplicitArgumentType<CommandSender, CommandContext<CommandSender>> {

    private static final CommandContextArgument INSTANCE = new CommandContextArgument();

    public static CommandContextArgument commandContext() {
        return INSTANCE;
    }


    private CommandContextArgument() {}

    @Override
    public CommandContext<CommandSender> parse(CommandContext<CommandSender> ctx) {
        return ctx;
    }
}
