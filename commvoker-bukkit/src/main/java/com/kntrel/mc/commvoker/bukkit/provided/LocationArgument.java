package com.kntrel.mc.commvoker.bukkit.provided;

import com.kntrel.mc.commvoker.argument.type.ContextualArgumentType;
import com.kntrel.mc.commvoker.bukkit.internal.CraftBukkitAccessors;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.Position;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;


public class LocationArgument implements ContextualArgumentType<CommandSender, Coordinates, Location> {

    public static LocationArgument location() {
        return new LocationArgument(Vec3Argument.vec3());
    }


    private final Vec3Argument delegate_;


    private LocationArgument(Vec3Argument delegate) {
        this.delegate_ = delegate;
    }

    @Override
    public Coordinates parse(StringReader reader) throws CommandSyntaxException {
        return this.delegate_.parse(reader);
    }

    @Override
    public Location contextualize(CommandContext<CommandSender> context, Coordinates subject) {
        CommandSourceStack css = (CommandSourceStack) CraftBukkitAccessors.getCommandSourceStack(context.getSource());
        Position pos = subject.getPosition(css);
        return new Location(null, pos.x(), pos.y(), pos.z());
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return this.delegate_.listSuggestions(context, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return this.delegate_.getExamples();
    }
}
