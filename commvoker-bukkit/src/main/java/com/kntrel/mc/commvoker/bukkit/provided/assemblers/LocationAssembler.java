package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.ComposedAssembler;
import com.kntrel.util.tuple.Pair;
import com.kntrel.util.tuple.impl.SimplePair;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import java.util.ArrayList;
import java.util.List;

public class LocationAssembler implements ComposedAssembler<CommandSender, Location> {


    //FACTORY
    public static LocationAssembler location() {
        return new LocationAssembler(VectorAssembler.vector(), WorldAssembler.world());
    }
    public static LocationAssembler locationInferredWorld() {
        return new LocationAssembler(VectorAssembler.vector());
    }
    public static LocationAssembler senderLocation() {
        return new LocationAssembler();
    }


    //FIELDS
    private final Assembler<? super CommandSender, Vector> vectorAssembler_;
    private final Assembler<? super CommandSender, World> worldAssembler_;


    //CONSTRUCTORS
    private LocationAssembler(Assembler<? super CommandSender, Vector> vectorAssembler, Assembler<? super CommandSender, World> worldAssembler) {
        this.vectorAssembler_ = vectorAssembler;
        this.worldAssembler_ = worldAssembler;
    }
    private LocationAssembler(Assembler<? super CommandSender, Vector> vectorAssembler) {
        this(vectorAssembler, null);
    }
    private LocationAssembler() {
        this(null, null);
    }


    //IMPLEMENTATION
    @Override
    public List<Pair<Assembler<? super CommandSender, ?>, SuggestionProvider<? super CommandSender>>> composedOf() {
        List<Pair<Assembler<? super CommandSender, ?>, SuggestionProvider<? super CommandSender>>> out = new ArrayList<>(2);
        if (this.vectorAssembler_ != null) {
            out.add(new SimplePair<>(this.vectorAssembler_, null));
        }
        if (this.worldAssembler_ != null) {
            out.add(new SimplePair<>(this.worldAssembler_, null));
        }
        return out;
    }
    @Override
    public Location compose(CommandContext<? extends CommandSender> ctx, Object[] objects) {
        CommandSender sender = ctx.getSource();

        if (objects.length < 1) {
            if (sender instanceof Entity e) {
                return e.getLocation();
            }
            return new Location(sender.getServer().getWorlds().iterator().next(), 0, 0, 0);
        }

        Vector v = (Vector) objects[0];
        World w = null;
        if (objects.length > 1) {
            w = (World) objects[1];
        } else if (sender instanceof Entity e) {
            w = e.getWorld();
        }

        return new Location(w, v.getX(), v.getY(), v.getZ());
    }
}
