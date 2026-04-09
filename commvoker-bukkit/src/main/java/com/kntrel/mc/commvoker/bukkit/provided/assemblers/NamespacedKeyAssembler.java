package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.TransformAssembler;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.resources.Identifier;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;

public class NamespacedKeyAssembler implements TransformAssembler<CommandSender, Identifier, NamespacedKey> {

    private static final NamespacedKeyAssembler INSTANCE = new NamespacedKeyAssembler();

    public static NamespacedKeyAssembler namespacedKey() {
        return INSTANCE;
    }


    private NamespacedKeyAssembler() {}


    @Override
    public Assembler<? super CommandSender, ? extends Identifier> delegate() {
        return Assembler.ofArgumentType(IdentifierArgument.id());
    }

    @Override
    public NamespacedKey compose(ExecutionContext<? extends CommandSender> ctx, Identifier object) {
        return CraftNamespacedKey.fromMinecraft(object);
    }
}
