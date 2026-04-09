package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.AssemblerHook;
import com.kntrel.mc.commvoker.assembler.AssemblyException;
import com.kntrel.mc.commvoker.assembler.ComposedAssembler;
import com.kntrel.mc.commvoker.assembler.TransformAssembler;
import com.kntrel.mc.commvoker.bukkit.internal.CraftBukkitAccessors;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.players.NameAndId;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.profile.PlayerProfile;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public class GameProfileAssembler<T> implements ComposedAssembler<CommandSender, List<T>> {

    public static GameProfileAssembler<PlayerProfile> profiles() {
        return new GameProfileAssembler<>(GameProfileAssembler::toPlayerProfile);
    }

    public static Assembler<CommandSender, Optional<PlayerProfile>> profile() {
        return new OptionalAssembler<>(profiles(), "player profile");
    }

    public static Assembler<CommandSender, PlayerProfile> requiredProfile() {
        return new RequiredAssembler<>(profile(), "player profile");
    }

    public static GameProfileAssembler<OfflinePlayer> offlinePlayers() {
        return new GameProfileAssembler<>(GameProfileAssembler::toOfflinePlayer);
    }

    public static Assembler<CommandSender, Optional<OfflinePlayer>> offlinePlayer() {
        return new OptionalAssembler<>(offlinePlayers(), "offline player");
    }

    public static Assembler<CommandSender, OfflinePlayer> requiredOfflinePlayer() {
        return new RequiredAssembler<>(offlinePlayer(), "offline player");
    }


    private static PlayerProfile toPlayerProfile(CommandSender sender, NameAndId profile) {
        if (profile.id() != null && profile.name() != null) {
            return sender.getServer().createPlayerProfile(profile.id(), profile.name());
        }
        if (profile.id() != null) {
            return sender.getServer().createPlayerProfile(profile.id());
        }
        if (profile.name() != null) {
            return sender.getServer().createPlayerProfile(profile.name());
        }
        throw new IllegalStateException("Profile is missing both UUID and name");
    }

    private static OfflinePlayer toOfflinePlayer(CommandSender sender, NameAndId profile) {
        if (profile.id() != null) {
            return sender.getServer().getOfflinePlayer(profile.id());
        }
        if (profile.name() != null) {
            return sender.getServer().getOfflinePlayer(profile.name());
        }
        throw new IllegalStateException("Profile is missing both UUID and name");
    }


    private static class OptionalAssembler<S, T, C extends Collection<T>> implements TransformAssembler<S, C, Optional<T>> {

        private final Assembler<? super S, ? extends C> delegate_;
        private final String label_;

        private OptionalAssembler(Assembler<? super S, ? extends C> delegate, String label) {
            this.delegate_ = delegate;
            this.label_ = label;
        }

        @Override
        public Assembler<? super S, ? extends C> delegate() {
            return this.delegate_;
        }

        @Override
        public Optional<T> compose(ExecutionContext<? extends S> ctx, C collection) throws AssemblyException {
            if (collection.isEmpty()) {
                return Optional.empty();
            }
            if (collection.size() > 1) {
                throw new AssemblyException("Expected a single " + this.label_);
            }
            return Optional.of(collection.iterator().next());
        }
    }

    private static class RequiredAssembler<S, T> implements TransformAssembler<S, Optional<T>, T> {

        private final Assembler<? super S, ? extends Optional<T>> delegate_;
        private final String label_;

        private RequiredAssembler(Assembler<? super S, ? extends Optional<T>> delegate, String label) {
            this.delegate_ = delegate;
            this.label_ = label;
        }

        @Override
        public Assembler<? super S, ? extends Optional<T>> delegate() {
            return this.delegate_;
        }

        @Override
        public T compose(ExecutionContext<? extends S> ctx, Optional<T> object) throws AssemblyException {
            if (object.isPresent()) {
                return object.get();
            }
            throw new AssemblyException("Missing " + this.label_);
        }
    }


    private final BiFunction<CommandSender, NameAndId, T> mapper_;


    private GameProfileAssembler(BiFunction<CommandSender, NameAndId, T> mapper) {
        this.mapper_ = mapper;
    }


    @Override
    public void composedOf(AssemblerHook<CommandSender> hook) {
        hook.hook("gameProfileArg", Assembler.ofArgumentType(GameProfileArgument.gameProfile()));
    }

    @Override
    public List<T> assemble(ExecutionContext<? extends CommandSender> ctx) throws AssemblyException {
        GameProfileArgument.Result result = ctx.component("gameProfileArg", GameProfileArgument.Result.class);
        Collection<NameAndId> profiles;
        try {
            profiles = result.getNames(CraftBukkitAccessors.commandSourceStack(ctx.source()));
        } catch (CommandSyntaxException e) {
            throw new AssemblyException(e.getMessage());
        }

        return profiles.stream()
                .map(profile -> this.mapper_.apply(ctx.source(), profile))
                .toList();
    }
}
