package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.TransformAssembler;
import net.minecraft.commands.arguments.coordinates.SwizzleArgument;
import net.minecraft.core.Direction;
import org.bukkit.Axis;
import org.bukkit.command.CommandSender;
import java.util.EnumSet;

public class SwizzleAssembler implements TransformAssembler<CommandSender, EnumSet<Direction.Axis>, EnumSet<Axis>> {

    private static final SwizzleAssembler INSTANCE = new SwizzleAssembler();

    public static SwizzleAssembler swizzle() {
        return INSTANCE;
    }


    private SwizzleAssembler() {}


    @Override
    public Assembler<? super CommandSender, ? extends EnumSet<Direction.Axis>> delegate() {
        return Assembler.ofArgumentType(SwizzleArgument.swizzle());
    }

    @Override
    public EnumSet<Axis> compose(ExecutionContext<? extends CommandSender> ctx, EnumSet<Direction.Axis> object) {
        EnumSet<Axis> axes = EnumSet.noneOf(Axis.class);
        object.forEach(axis -> axes.add(Axis.valueOf(axis.name())));
        return axes;
    }
}
