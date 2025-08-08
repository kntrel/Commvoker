package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.ComposedAssembler;
import com.kntrel.mc.commvoker.provided.assemblers.StringAssembler;
import com.kntrel.util.tuple.Pair;
import com.kntrel.util.tuple.impl.SimplePair;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WorldAssembler implements ComposedAssembler<CommandSender, World> {

    private static final WorldAssembler EXPLICIT_INSTANCE = new WorldAssembler(false),
                                        IMPLICIT_INSTANCE = new WorldAssembler(true);

    //FACTORY
    public static WorldAssembler world() {
        return EXPLICIT_INSTANCE;
    }
    public static WorldAssembler senderWorld() {
        return IMPLICIT_INSTANCE;
    }



    //CONSTRUCTOR
    private final boolean implicit_;

    private WorldAssembler(boolean implicit) {
        this.implicit_ = implicit;
    }


    //IMPLEMENTATION
    @Override
    public List<Pair<Assembler<? super CommandSender, ?>, SuggestionProvider<? super CommandSender>>> delegates() {
        if (this.implicit_) { return Collections.emptyList(); }
        return List.of(new SimplePair<>(StringAssembler.word(), this::getSuggestions));
    }

    @Override
    public World compose(CommandContext<? extends CommandSender> ctx, Object[] objects) {
        CommandSender sender = ctx.getSource();

        if (this.implicit_) {
            if (sender instanceof Entity w) {
                return w.getWorld();
            }
            return sender.getServer().getWorlds().get(0);
        }

        return ctx.getSource().getServer().getWorld((String) objects[0]);
    }

    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSender> context, SuggestionsBuilder builder) {
        context.getSource().getServer().getWorlds().forEach(w -> builder.suggest(w.getName()));
        return builder.buildFuture();
    }
}
