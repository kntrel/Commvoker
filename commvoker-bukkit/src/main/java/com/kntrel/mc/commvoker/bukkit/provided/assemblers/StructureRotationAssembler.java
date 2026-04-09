package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.TransformAssembler;
import net.minecraft.commands.arguments.TemplateRotationArgument;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.command.CommandSender;

public class StructureRotationAssembler implements TransformAssembler<CommandSender, net.minecraft.world.level.block.Rotation, StructureRotation> {

    private static final StructureRotationAssembler INSTANCE = new StructureRotationAssembler();

    public static StructureRotationAssembler structureRotation() {
        return INSTANCE;
    }


    private StructureRotationAssembler() {}


    @Override
    public Assembler<? super CommandSender, ? extends net.minecraft.world.level.block.Rotation> delegate() {
        return Assembler.ofArgumentType(TemplateRotationArgument.templateRotation());
    }

    @Override
    public StructureRotation compose(ExecutionContext<? extends CommandSender> ctx, net.minecraft.world.level.block.Rotation object) {
        return StructureRotation.valueOf(object.name());
    }
}
