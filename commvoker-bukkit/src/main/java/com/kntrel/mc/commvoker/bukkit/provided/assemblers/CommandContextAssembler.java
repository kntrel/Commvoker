package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.assembler.ImplicitAssembler;
import com.mojang.brigadier.context.CommandContext;
import org.bukkit.command.CommandSender;

public class CommandContextAssembler implements ImplicitAssembler<CommandSender, CommandContext<CommandSender>> {

    //FACTORY
    private static final CommandContextAssembler INSTANCE = new CommandContextAssembler();
    public static CommandContextAssembler commandContext() {
        return INSTANCE;
    }


    //CONSTRUCTOR
    private CommandContextAssembler() {}


    //IMPLEMENTATION
    @Override @SuppressWarnings("unchecked")
    public CommandContext<CommandSender> apply(CommandContext<? extends CommandSender> ctx) {
        return (CommandContext<CommandSender>) ctx;
    }
}
