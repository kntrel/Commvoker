package com.kntrel.mc.commvoker.bukkit.requirement;

import com.kntrel.mc.commvoker.requirement.AnnotatedRequirement;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

class ConsoleRequirement implements AnnotatedRequirement<CommandSender, OnlyConsole> {
    @Override
    public boolean test(CommandSender source, OnlyConsole annotation) {
        return source instanceof ConsoleCommandSender;
    }
}
