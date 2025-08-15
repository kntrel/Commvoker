package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.ComposedAssembler;
import com.kntrel.mc.commvoker.bukkit.internal.CraftBukkitAccessors;
import com.kntrel.util.tuple.Pair;
import com.kntrel.util.tuple.impl.SimplePair;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.world.phys.Vec3;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;
import java.util.List;

public class VectorAssembler implements ComposedAssembler<CommandSender, Vector> {

    private static final VectorAssembler INSTANCE = new VectorAssembler();

    public static VectorAssembler vector() {
        return INSTANCE;
    }


    //CONSTRUCTOR
    private VectorAssembler() {}


    //IMPLEMENTATION
    @Override
    public List<Pair<Assembler<? super CommandSender, ?>, SuggestionProvider<? super CommandSender>>> composedOf() {
        return List.of(new SimplePair<>(Assembler.ofArgumentType(Vec3Argument.vec3()), null));
    }
    @Override
    public Vector compose(CommandContext<? extends CommandSender> ctx, Object[] objects) {
        Coordinates coordinates = (Coordinates) objects[0];
        Vec3 vec3 = coordinates.getPosition((CommandSourceStack) CraftBukkitAccessors.getCommandSourceStack(ctx.getSource()));

        return new Vector(vec3.x(), vec3.y(), vec3.z());
    }
}