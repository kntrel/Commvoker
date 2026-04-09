package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.TransformAssembler;
import net.minecraft.commands.arguments.TemplateMirrorArgument;
import org.bukkit.block.structure.Mirror;
import org.bukkit.command.CommandSender;

public class MirrorAssembler implements TransformAssembler<CommandSender, net.minecraft.world.level.block.Mirror, Mirror> {

    private static final MirrorAssembler INSTANCE = new MirrorAssembler();

    public static MirrorAssembler mirror() {
        return INSTANCE;
    }


    private MirrorAssembler() {}


    @Override
    public Assembler<? super CommandSender, ? extends net.minecraft.world.level.block.Mirror> delegate() {
        return Assembler.ofArgumentType(TemplateMirrorArgument.templateMirror());
    }

    @Override
    public Mirror compose(ExecutionContext<? extends CommandSender> ctx, net.minecraft.world.level.block.Mirror object) {
        return Mirror.valueOf(object.name());
    }
}
