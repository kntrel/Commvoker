package com.kntrel.mc.commvoker.provided.assemblers;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.AssemblyException;
import com.kntrel.mc.commvoker.assembler.TransformAssembler;

public class CharacterAssembler implements TransformAssembler<Object, String, Character> {

    private static final CharacterAssembler INSTANCE = new CharacterAssembler();

    public static CharacterAssembler character() {
        return INSTANCE;
    }


    private CharacterAssembler() {}


    @Override
    public Assembler<? super Object, ? extends String> delegate() {
        return StringAssembler.word();
    }

    @Override
    public Character compose(ExecutionContext<?> ctx, String object) throws AssemblyException {
        if (object.length() != 1) {
            throw new AssemblyException("Expected a single character", object);
        }
        return object.charAt(0);
    }
}
