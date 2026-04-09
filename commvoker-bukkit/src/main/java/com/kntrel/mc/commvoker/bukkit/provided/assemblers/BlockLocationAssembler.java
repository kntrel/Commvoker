package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.AssemblerHook;
import com.kntrel.mc.commvoker.assembler.ComposedAssembler;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.util.BlockVector;

public class BlockLocationAssembler implements ComposedAssembler<CommandSender, Location> {

    public static BlockLocationAssembler location() {
        return new BlockLocationAssembler(BlockVectorAssembler.blockVector(), WorldAssembler.world());
    }

    public static BlockLocationAssembler locationInferredWorld() {
        return new BlockLocationAssembler(BlockVectorAssembler.blockVector());
    }


    private final Assembler<? super CommandSender, BlockVector> vectorAssembler_;
    private final Assembler<? super CommandSender, World> worldAssembler_;


    private BlockLocationAssembler(Assembler<? super CommandSender, BlockVector> vectorAssembler, Assembler<? super CommandSender, World> worldAssembler) {
        this.vectorAssembler_ = vectorAssembler;
        this.worldAssembler_ = worldAssembler;
    }

    private BlockLocationAssembler(Assembler<? super CommandSender, BlockVector> vectorAssembler) {
        this(vectorAssembler, null);
    }


    @Override
    public void composedOf(AssemblerHook<CommandSender> hook) {
        hook.hook("vecArg", this.vectorAssembler_);
        if (this.worldAssembler_ != null) {
            hook.hook("worldArg", this.worldAssembler_);
        }
    }

    @Override
    public Location assemble(ExecutionContext<? extends CommandSender> ctx) {
        CommandSender sender = ctx.source();
        BlockVector vector = ctx.component("vecArg", BlockVector.class);

        World world = null;
        if (ctx.hasComponent("worldArg")) {
            world = ctx.component("worldArg", World.class);
        } else if (sender instanceof Entity entity) {
            world = entity.getWorld();
        }

        return new Location(world, vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }
}
