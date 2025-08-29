package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.argument.binding.CommandTemplate;
import com.kntrel.mc.commvoker.command.CommandDefinition;
import com.kntrel.mc.commvoker.command.CommandToken;
import com.kntrel.mc.commvoker.provided.annotations.Word;
import com.kntrel.mc.commvoker.argument.context.ArgumentContext;
import com.kntrel.mc.commvoker.argument.binder.ArgumentBinder;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.provided.ArgumentBindings;
import com.kntrel.mc.commvoker.exception.NoSuchArgumentBindingException;
import com.kntrel.util.Priority;
import com.mojang.brigadier.arguments.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ArgumentResolverImplTest {

    /* ----------------------------------------------------------
     *  SET‑UP UTILITIES
     * --------------------------------------------------------- */

    // A fake command method supplying most parameter shapes we need
    @SuppressWarnings("unused")
    private void dummy(
            String plain,
            @Word String word,
            int primitiveInt,
            Integer boxedInt,
            double primitiveDouble,
            List<Integer> intList,
            Set<Boolean> boolSet,
            String greedy                // ← last token
    ) {}

    /** Build a minimal CommandDefinition `<plain> <word> <int> <box> <dbl> <list> <set> <greedyString>` */
    private static final CommandDefinition CMD_DEF = new CommandDefinition(new CommandToken[]{
            CommandToken.argument("plain"),
            CommandToken.argument("word"),
            CommandToken.argument("prim"),
            CommandToken.argument("boxed"),
            CommandToken.argument("dbl"),
            CommandToken.argument("list"),
            CommandToken.argument("set"),
            CommandToken.argument("greedy")
    });

    /** Convenience for building an ArgumentContext for the given parameter index. */
    private static ArgumentContext ctx(Method m, int paramIdx) {
        Parameter p = m.getParameters()[paramIdx];
        return new ArgumentContext(
                p,
                p.getParameterizedType(),
                m,
                paramIdx,
                CMD_DEF,
                paramIdx,
                List.of()
        );
    }

    /* ----------------------------------------------------------
     *  TESTS
     * --------------------------------------------------------- */

    private final Method DUMMY;
    private final ArgumentResolverImpl<Object> resolver = new ArgumentResolverImpl<>();

    ArgumentResolverImplTest() throws NoSuchMethodException {
        DUMMY = ArgumentResolverImplTest.class
                .getDeclaredMethod("dummy",
                        String.class, String.class, int.class, Integer.class,
                        double.class, List.class, Set.class, String.class);
    }

    @BeforeEach
    public void init() {
        ArgumentBindings.all().forEach(this.resolver::register);
    }

    @Test
    void resolvesPlainString_toStringArgumentType() {
        var desc = resolver.resolve(ctx(DUMMY, 0));
        StringArgumentType stringArgumentType = assertArgumentTypeNode(StringArgumentType.class, desc.template());
        assertNotEquals(stringArgumentType.getType(), StringArgumentType.StringType.GREEDY_PHRASE);
    }

    @Test
    void resolvesWordAnnotatedString_toWordArgumentType() {
        var desc = resolver.resolve(ctx(DUMMY, 1));
        StringArgumentType stringArgumentType = assertArgumentTypeNode(StringArgumentType.class, desc.template());
        assertEquals(StringArgumentType.StringType.SINGLE_WORD, stringArgumentType.getType());
    }

    @Test
    void resolvesPrimitiveInt_viaPrimitiveBinding() {
        var desc = resolver.resolve(ctx(DUMMY, 2));
        assertArgumentTypeNode(IntegerArgumentType.class, desc.template());
    }

    @Test
    void resolvesBoxedInteger_viaINTEGERBinding() {
        var desc = resolver.resolve(ctx(DUMMY, 3));
        assertArgumentTypeNode(IntegerArgumentType.class, desc.template());
    }

    @Test
    void resolvesPrimitiveDouble_viaPrimitiveBinding() {
        var desc = resolver.resolve(ctx(DUMMY, 4));
        assertArgumentTypeNode(DoubleArgumentType.class, desc.template());
    }

    @Test
    void lastStringTokenBecomesGreedy() {
        var desc = resolver.resolve(ctx(DUMMY, 7));
        StringArgumentType stringArgumentType =  assertArgumentTypeNode(StringArgumentType.class, desc.template());
        assertEquals(StringArgumentType.StringType.GREEDY_PHRASE, stringArgumentType.getType());
    }

    @Test
    void higherPriorityBindingWins() {
        // custom binding: map String.class to literal "OVERRIDE"
        StringArgumentType overwrite = StringArgumentType.word();

        var override = ArgumentBinder.argumentAssembler(() -> Assembler.ofArgumentType(overwrite))
                .toClass(String.class)
                .withPriority(Priority.HIGH)
                .bind();
        resolver.register(override);

        ArgumentContext ctx = ctx(DUMMY, 0);
        var desc = resolver.resolve(ctx);
        ArgumentType<?> argType = assertArgumentTypeNode(ArgumentType.class, desc.template());
        assertSame(overwrite, argType);
    }

    @Test
    void throwsWhenNoBindingMatches() throws NoSuchMethodException {
        Method m = getClass().getDeclaredMethod("noBinding", UUID.class);
        var badCtx = new ArgumentContext(
                m.getParameters()[0],
                UUID.class,
                m,
                0,
                new CommandDefinition(new CommandToken[]{ CommandToken.argument("uuid") }),
                0,
                List.of()
        );
        assertThrows(NoSuchArgumentBindingException.class,
                () -> resolver.resolve(badCtx));
    }

    // helper for the above test
    @SuppressWarnings("unused") private void noBinding(UUID id) {}


    private static <T extends ArgumentType<?>> T assertArgumentTypeNode(Class<T> argumentTypeClass, CommandTemplate<?> tmpl) {
        List<? extends CommandTemplate.Node<?>> trees = tmpl.trees();
        assertEquals(1, trees.size());
        CommandTemplate.Node<?> node = trees.getFirst();
        return assertArgumentTypeNode(argumentTypeClass, node);
    }
    private static <T extends ArgumentType<?>> T assertArgumentTypeNode(Class<T> argumentTypeClass, CommandTemplate.Node<?> node) {
        CommandTemplate.Argument<?> arg = assertInstanceOf(CommandTemplate.Argument.class, node);
        return assertInstanceOf(argumentTypeClass, arg.argumentType());
    }
}
