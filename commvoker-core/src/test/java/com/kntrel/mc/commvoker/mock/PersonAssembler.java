package com.kntrel.mc.commvoker.mock;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.BiComposedAssembler;
import com.kntrel.mc.commvoker.provided.assemblers.IntegerAssembler;
import com.kntrel.mc.commvoker.provided.assemblers.StringAssembler;

public class PersonAssembler implements BiComposedAssembler<Object, String, Integer, Person> {
    @Override
    public Assembler<? super Object, ? extends String> firstDelegate() {
        return StringAssembler.string();
    }

    @Override
    public Assembler<? super Object, ? extends Integer> secondDelegate() {
        return IntegerAssembler.integer(0, 120);
    }

    @Override
    public Person compose(ExecutionContext<?> ctx, String first, Integer second) {
        return new Person(first, second);
    }
}
