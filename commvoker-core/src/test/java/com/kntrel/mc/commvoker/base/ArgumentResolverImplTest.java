package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.annotation.Word;
import com.kntrel.mc.commvoker.argument.ArgumentContext;
import com.kntrel.mc.commvoker.argument.bind.ArgumentBinder;
import com.kntrel.mc.commvoker.argument.bind.ArgumentGatherer;
import com.kntrel.mc.commvoker.argument.type.ContextualArgumentType;
import com.kntrel.mc.commvoker.builtin.ArgumentBindings;
import com.kntrel.mc.commvoker.builtin.argumentType.CollectionArgumentType;
import com.kntrel.mc.commvoker.command.CommandDefinition;
import com.kntrel.mc.commvoker.command.CommandToken;
import com.kntrel.mc.commvoker.exception.NoSuchArgumentBindingException;
import com.kntrel.util.Priority;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
                new ArgumentType[]{}
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
        StringArgumentType stringArgumentType = assertInstanceOf(StringArgumentType.class, desc.argumentType());
        assertNotEquals(stringArgumentType.getType(), StringArgumentType.StringType.GREEDY_PHRASE);
    }

    @Test
    void resolvesWordAnnotatedString_toWordArgumentType() {
        var desc = resolver.resolve(ctx(DUMMY, 1));
        StringArgumentType stringArgumentType = assertInstanceOf(StringArgumentType.class, desc.argumentType());
        assertEquals(StringArgumentType.StringType.SINGLE_WORD, stringArgumentType.getType());
    }

    @Test
    void resolvesPrimitiveInt_viaPrimitiveBinding() {
        var desc = resolver.resolve(ctx(DUMMY, 2));
        assertInstanceOf(IntegerArgumentType.class, desc.argumentType());
    }

    @Test
    void resolvesBoxedInteger_viaINTEGERBinding() {
        var desc = resolver.resolve(ctx(DUMMY, 3));
        assertInstanceOf(IntegerArgumentType.class, desc.argumentType());
    }

    @Test
    void resolvesPrimitiveDouble_viaPrimitiveBinding() {
        var desc = resolver.resolve(ctx(DUMMY, 4));
        assertInstanceOf(DoubleArgumentType.class, desc.argumentType());
    }

    @Test
    void resolvesListOfIntegers_toCollectionList() {
        var desc = resolver.resolve(ctx(DUMMY, 5));
        CollectionArgumentType<?, ?> collectionArgumentType = assertInstanceOf(CollectionArgumentType.class, desc.argumentType());
        assertEquals(List.class, collectionArgumentType.getCollectionType());
    }

    @Test
    void resolvesSetOfBooleans_toCollectionSet() {
        var desc = resolver.resolve(ctx(DUMMY, 6));
        CollectionArgumentType<?, ?> collectionArgumentType = assertInstanceOf(CollectionArgumentType.class, desc.argumentType());
        assertEquals(Set.class, collectionArgumentType.getCollectionType());
    }

    @Test
    void lastStringTokenBecomesGreedy() {
        var desc = resolver.resolve(ctx(DUMMY, 7));
        StringArgumentType stringArgumentType =  assertInstanceOf(StringArgumentType.class, desc.argumentType());
        assertEquals(StringArgumentType.StringType.GREEDY_PHRASE, stringArgumentType.getType());
    }

    @Test
    void higherPriorityBindingWins() {
        // custom binding: map String.class to literal "OVERRIDE"
        StringArgumentType artType = StringArgumentType.word();

        var override = ArgumentBinder.argument(() -> artType)
                .toClass(String.class)
                .withPriority(Priority.HIGH)
                .bind().define();
        resolver.register(override);

        ArgumentContext ctx = ctx(DUMMY, 0);
        var desc = resolver.resolve(ctx); // plain String param again
        ArgumentGatherer<Object> gatherer = new ArgumentGatherer<>(ctx, resolver, new PriorityQueue<>());
        assertSame(artType, desc.argumentType());
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
                new ArgumentType[0]
        );
        assertThrows(NoSuchArgumentBindingException.class,
                () -> resolver.resolve(badCtx));
    }

    // helper for the above test
    @SuppressWarnings("unused") private void noBinding(UUID id) {}
}
