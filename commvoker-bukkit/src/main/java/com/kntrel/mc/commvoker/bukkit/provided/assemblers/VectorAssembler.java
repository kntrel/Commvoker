package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.AssemblerHook;
import com.kntrel.mc.commvoker.assembler.ComposedAssembler;
import com.kntrel.mc.commvoker.bukkit.internal.CraftBukkitAccessors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.world.phys.Vec3;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;

public class VectorAssembler implements ComposedAssembler<CommandSender, Vector> {

    private static final VectorAssembler INSTANCE = new VectorAssembler();

    public static VectorAssembler vector() {
        return INSTANCE;
    }


    //CONSTRUCTOR
    private VectorAssembler() {}


    //IMPLEMENTATION
    @Override
    public void composedOf(AssemblerHook<CommandSender> hooK) {
        hooK.hook("vecArg", Assembler.ofArgumentType(Vec3Argument.vec3()));
    }
    @Override
    public Vector contextualize(ExecutionContext<? extends CommandSender> ctx) {
        Coordinates coordinates = ctx.component("vecArg", Coordinates.class);
        Vec3 vec3 = coordinates.getPosition((CommandSourceStack) CraftBukkitAccessors.getCommandSourceStack(ctx.source()));

        return new Vector(vec3.x(), vec3.y(), vec3.z());
    }
}