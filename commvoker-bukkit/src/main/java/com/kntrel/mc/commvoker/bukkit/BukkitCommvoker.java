package com.kntrel.mc.commvoker.bukkit;

import com.kntrel.mc.commvoker.argument.ArgumentRegistry;
import com.kntrel.mc.commvoker.base.BaseCommvoker;
import com.kntrel.mc.commvoker.bukkit.provided.BukkitArgumentBindings;
import com.kntrel.mc.commvoker.bukkit.provided.callbacks.*;
import com.kntrel.mc.commvoker.callback.ReturnCallback;
import com.mojang.brigadier.CommandDispatcher;
import org.bukkit.command.CommandSender;

import java.util.List;

public class BukkitCommvoker extends BaseCommvoker<CommandSender> {
    public BukkitCommvoker(CommandDispatcher<CommandSender> commandDispatcher) {
        super(CommandSender.class, commandDispatcher);

        ArgumentRegistry<CommandSender> registry = this.getArgumentRegistry();
        registry.register(BukkitArgumentBindings.all());

        List.<ReturnCallback<? super CommandSender, ?>>of(
                new CommandResultCallback(),
                new MessageCallback(),
                new PlayerStringMapCallback(),
                new PlayerMessageMapCallback(),
                new StringArrayCallback(),
                new StringListCallback(),
                new StringCallBack()
        ).forEach(this::registerCallback);
    }
}
