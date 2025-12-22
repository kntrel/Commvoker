package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.AssemblerHook;
import com.kntrel.mc.commvoker.assembler.ComposedAssembler;
import com.kntrel.mc.commvoker.provided.assemblers.StringAssembler;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import java.util.concurrent.CompletableFuture;

public class WorldAssembler implements ComposedAssembler<CommandSender, World> {

    private static final WorldAssembler INSTANCE = new WorldAssembler();

    //FACTORY
    public static WorldAssembler world() {
        return INSTANCE;
    }



    //CONSTRUCTOR
    private WorldAssembler() {}


    //IMPLEMENTATION
    public CompletableFuture<Suggestions> suggest(ExecutionContext<? extends CommandSender> context, SuggestionsBuilder builder) {
        context.source().getServer().getWorlds().forEach(w -> builder.suggest(w.getName()));
        return builder.buildFuture();
    }
    @Override
    public void composedOf(AssemblerHook<CommandSender> hooK) {
        hooK.hook("worldArg", StringAssembler.word()).suggests(this::suggest);
    }
    @Override
    public World assemble(ExecutionContext<? extends CommandSender> ctx) {
        return ctx.source().getServer().getWorld(ctx.component("worldArg", String.class));
    }
}
