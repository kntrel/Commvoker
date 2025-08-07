package com.kntrel.mc.commvoker.bukkit;

import com.kntrel.mc.commvoker.argument.ArgumentRegistry;
import com.kntrel.mc.commvoker.base.BaseCommvoker;
import com.kntrel.mc.commvoker.bukkit.provided.BukkitArgumentBindings;
import com.mojang.brigadier.CommandDispatcher;
import org.bukkit.command.CommandSender;

public class BukkitCommvoker extends BaseCommvoker<CommandSender> {
    public BukkitCommvoker(CommandDispatcher<CommandSender> commandDispatcher) {
        super(commandDispatcher);

        ArgumentRegistry<CommandSender> registry = this.getArgumentRegistry();
        registry.register(BukkitArgumentBindings.all());
    }
}
