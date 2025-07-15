package com.kntrel.mc.commvoker;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import org.bukkit.Server;
import java.lang.reflect.Method;


final class ReflectiveDispatcherAccessor {

    private ReflectiveDispatcherAccessor() {}


    @SuppressWarnings("unchecked")
    public static CommandDispatcher<CommandSourceStack> getDispatcher(Server bukkitServer) {
        try {
            /* CraftServer → MinecraftServer */
            Method getServer = bukkitServer.getClass().getDeclaredMethod("getServer");
            Object nmsServer = getServer.invoke(bukkitServer);

            /* MinecraftServer → Commands */
            Method getCommands = nmsServer.getClass().getDeclaredMethod("getCommands");
            Object commands = getCommands.invoke(nmsServer);

            /* Commands → CommandDispatcher */
            Method getDispatcher = commands.getClass().getDeclaredMethod("getDispatcher");
            return (CommandDispatcher<CommandSourceStack>) getDispatcher.invoke(commands);

        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Unable to resolve CommandDispatcher reflectively", ex);
        }
    }
}
