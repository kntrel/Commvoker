package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.assembler.ImplicitAssembler;
import com.mojang.brigadier.context.CommandContext;
import org.bukkit.command.CommandSender;

public class CommandSenderAssembler implements ImplicitAssembler<CommandSender, CommandSender> {

    //FACTORY
    private static final CommandSenderAssembler INSTANCE = new CommandSenderAssembler();
    public static CommandSenderAssembler commandSender() {
        return INSTANCE;
    }


    //CONSTRUCTOR
    private CommandSenderAssembler() {}


    //IMPLEMENTATION
    @Override
    public CommandSender apply(CommandContext<? extends CommandSender> ctx) {
        return ctx.getSource();
    }
}
