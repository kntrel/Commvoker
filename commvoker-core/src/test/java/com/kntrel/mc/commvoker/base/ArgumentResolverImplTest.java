package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.annotation.Word;
import com.kntrel.mc.commvoker.argument.ArgumentContext;
import com.kntrel.mc.commvoker.argument.binder.ArgumentBinder;
import com.kntrel.mc.commvoker.argument.binder.ArgumentGatherer;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.command.CommandPattern;
import com.kntrel.mc.commvoker.command.CommandPatternToken;
import com.kntrel.mc.commvoker.provided.ArgumentBindings;
import com.kntrel.mc.commvoker.exception.NoSuchArgumentBindingException;
import com.kntrel.util.Priority;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.PriorityQueue;
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
    private static final CommandPattern CMD_DEF = new CommandPattern(new CommandPatternToken[]{
            CommandPatternToken.argument("plain"),
            CommandPatternToken.argument("word"),
            CommandPatternToken.argument("prim"),
            CommandPatternToken.argument("boxed"),
            CommandPatternToken.argument("dbl"),
            CommandPatternToken.argument("list"),
            CommandPatternToken.argument("set"),
            CommandPatternToken.argument("greedy")
    });

    /** Convenience for building an ArgumentContext for the given parameter index. */
    private static ArgumentContext ctx(Method m, int paramIdx) {
        Parameter p = m.getParameters()[paramIdx];
        return new ArgumentContext(
                p,
                p.getParameterizedType(),
                m,
                paramIdx,
                CMD_DEF
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
        StringArgumentType stringArgumentType = assertInstanceOf(StringArgumentType.class, desc.argumentNodes().iterator().next().argumentType());
        assertNotEquals(stringArgumentType.getType(), StringArgumentType.StringType.GREEDY_PHRASE);
    }

    @Test
    void resolvesWordAnnotatedString_toWordArgumentType() {
        var desc = resolver.resolve(ctx(DUMMY, 1));
        StringArgumentType stringArgumentType = assertInstanceOf(StringArgumentType.class, desc.argumentNodes().iterator().next().argumentType());
        assertEquals(StringArgumentType.StringType.SINGLE_WORD, stringArgumentType.getType());
    }

    @Test
    void resolvesPrimitiveInt_viaPrimitiveBinding() {
        var desc = resolver.resolve(ctx(DUMMY, 2));
        assertInstanceOf(IntegerArgumentType.class, desc.argumentNodes().iterator().next().argumentType());
    }

    @Test
    void resolvesBoxedInteger_viaINTEGERBinding() {
        var desc = resolver.resolve(ctx(DUMMY, 3));
        assertInstanceOf(IntegerArgumentType.class, desc.argumentNodes().iterator().next().argumentType());
    }

    @Test
    void resolvesPrimitiveDouble_viaPrimitiveBinding() {
        var desc = resolver.resolve(ctx(DUMMY, 4));
        assertInstanceOf(DoubleArgumentType.class, desc.argumentNodes().iterator().next().argumentType());
    }

    @Test
    void resolvesListOfIntegers_toCollectionList() {
        var desc = resolver.resolve(ctx(DUMMY, 5));
        CollectionArgumentType<?, ?> collectionArgumentType = assertInstanceOf(CollectionArgumentType.class, desc.argumentNodes().iterator().next().argumentType());
        assertEquals(List.class, collectionArgumentType.getCollectionType());
    }

    @Test
    void resolvesSetOfBooleans_toCollectionSet() {
        var desc = resolver.resolve(ctx(DUMMY, 6));
        CollectionArgumentType<?, ?> collectionArgumentType = assertInstanceOf(CollectionArgumentType.class, desc.argumentNodes().iterator().next().argumentType());
        assertEquals(Set.class, collectionArgumentType.getCollectionType());
    }

    @Test
    void lastStringTokenBecomesGreedy() {
        var desc = resolver.resolve(ctx(DUMMY, 7));
        StringArgumentType stringArgumentType =  assertInstanceOf(StringArgumentType.class, desc.argumentNodes().iterator().next().argumentType());
        assertEquals(StringArgumentType.StringType.GREEDY_PHRASE, stringArgumentType.getType());
    }

    @Test
    void higherPriorityBindingWins() {
        // custom binding: map String.class to literal "OVERRIDE"
        StringArgumentType artType = StringArgumentType.word();

        var override = ArgumentBinder.argumentAssembler(() -> Assembler.ofArgumentType(artType))
                .toClass(String.class)
                .withPriority(Priority.HIGH)
                .bind();
        resolver.register(override);

        ArgumentContext ctx = ctx(DUMMY, 0);
        var desc = resolver.resolve(ctx); // plain String param again
        ArgumentGatherer<Object> gatherer = new ArgumentGatherer<>(ctx, resolver, new PriorityQueue<>());
        assertSame(artType, desc.argumentNodes().iterator().next().argumentType());
    }

    @Test
    void throwsWhenNoBindingMatches() throws NoSuchMethodException {
        Method m = getClass().getDeclaredMethod("noBinding", UUID.class);
        var badCtx = new ArgumentContext(
                m.getParameters()[0],
                UUID.class,
                m,
                0,
                new CommandPattern(new CommandPatternToken[]{ CommandPatternToken.argument("uuid") })
        );
        assertThrows(NoSuchArgumentBindingException.class,
                () -> resolver.resolve(badCtx));
    }

    // helper for the above test
    @SuppressWarnings("unused") private void noBinding(UUID id) {}
}
