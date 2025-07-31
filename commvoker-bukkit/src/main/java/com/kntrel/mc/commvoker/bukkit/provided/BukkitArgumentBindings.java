package com.kntrel.mc.commvoker.bukkit.provided;


import com.kntrel.mc.commvoker.argument.bind.ArgumentBinder;
import com.kntrel.mc.commvoker.argument.bind.ArgumentBinding;
import com.kntrel.util.Constants;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.Collection;

public class BukkitArgumentBindings {


    public static final ArgumentBinding<CommandSender, ?>
    LOCATION = ArgumentBinder.contextual(LocationArgument::location)
            .toClass(Location.class)
            .bind();


    @SuppressWarnings("unchecked")
    public static Collection<ArgumentBinding<CommandSender, ?>> all() {
        return Constants.getAll(BukkitArgumentBindings.class, (Class<ArgumentBinding<CommandSender,?>>) (Class<?>) ArgumentBinding.class);
    }

}
