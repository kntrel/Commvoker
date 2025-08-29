package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.BiComposedAssembler;
import org.bukkit.command.CommandSender;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class BoundingBoxAssembler implements BiComposedAssembler<CommandSender, Vector, Vector, BoundingBox> {

    //FACTORY
    private static final BoundingBoxAssembler INSTANCE = new BoundingBoxAssembler();
    public static BoundingBoxAssembler boundingBox() {
        return INSTANCE;
    }


    //CONSTRUCTOR
    private BoundingBoxAssembler() {}


    //IMPLEMENTATION
    @Override
    public Assembler<? super CommandSender, ? extends Vector> firstDelegate() {
        return VectorAssembler.vector();
    }
    @Override
    public Assembler<? super CommandSender, ? extends Vector> secondDelegate() {
        return VectorAssembler.vector();
    }
    @Override
    public BoundingBox compose(ExecutionContext<? extends CommandSender> ctx, Vector vec1, Vector vec2) {
        return new BoundingBox(vec1.getX(), vec1.getY(), vec1.getZ(), vec2.getX(), vec2.getY(), vec2.getZ());
    }
}