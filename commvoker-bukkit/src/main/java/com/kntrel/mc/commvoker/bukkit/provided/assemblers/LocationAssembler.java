package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.argument.binding.Components;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.AssemblerHook;
import com.kntrel.mc.commvoker.assembler.ComposedAssembler;
import com.mojang.brigadier.context.CommandContext;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class LocationAssembler implements ComposedAssembler<CommandSender, Location> {


    //FACTORY
    public static LocationAssembler location() {
        return new LocationAssembler(VectorAssembler.vector(), WorldAssembler.world());
    }
    public static LocationAssembler locationInferredWorld() {
        return new LocationAssembler(VectorAssembler.vector());
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
    public void composedOf(AssemblerHook<CommandSender> hooK) {
        if (this.vectorAssembler_ != null) {
            hooK.hook("vecArg", this.vectorAssembler_);
        }
        if (this.worldAssembler_ != null) {
            hooK.hook("worldArg", this.worldAssembler_);
        }
    }

    @Override
    public Location contextualize(CommandContext<? extends CommandSender> ctx, Components components) {
        CommandSender sender = ctx.getSource();

        Vector v = components.get("vecArg", Vector.class);
        World w = null;
        if (components.has("worldArg")) {
            w = components.get("worldArg", World.class);
        } else if (sender instanceof Entity e) {
            w = e.getWorld();
        }

        return new Location(w, v.getX(), v.getY(), v.getZ());
    }


}
