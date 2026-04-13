package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.error.CommandExceptionHandler;
import com.kntrel.mc.commvoker.error.CommandExceptionResolver;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.LiteralMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class CommandExceptionResolverImplTest {

    private static class SubException extends IllegalArgumentException {
        public SubException(String msg) { super(msg); }
    }

    private CommandExceptionResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new CommandExceptionResolverImpl();
    }

    @Test
    void testRegisterAndResolveHandler() {
        resolver.registerHandler(IllegalArgumentException.class, e -> CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().create(e.getMessage()));
        CommandExceptionHandler<IllegalArgumentException> handler = resolver.registerHandler(IllegalArgumentException.class, e -> CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().create(e.getMessage()));

        assertNotNull(handler);
        assertDoesNotThrow(() -> handler.handle(new IllegalArgumentException("Test")));
    }

    @Test
    void testUnregisterHandler() {
        Function<IllegalArgumentException, CommandSyntaxException> handlerFunction = e -> CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().create(e.getMessage());
        CommandExceptionHandler<IllegalArgumentException> handler = resolver.registerHandler(IllegalArgumentException.class, handlerFunction);
        assertNotNull(handler);

        resolver.unregisterHandler(handler);

        CommandSyntaxException res = resolver.resolve(new IllegalArgumentException("Test"));
        assertNull(res);
    }

    @Test
    void testResolveWithoutHandler() {
        CommandSyntaxException res = resolver.resolve(new NullPointerException("No handler registered"));
        assertNull(res);
    }

    @Test
    void testResolveSubClass() {
        CommandExceptionHandler<IllegalArgumentException> handler = resolver.registerHandler(IllegalArgumentException.class, e -> CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().create(e.getMessage()));

        CommandSyntaxException res = resolver.resolve(new SubException("test"));
        assertNotNull(res);
    }

    @Test
    void testResolveWrappedCauseUsesMatchingCause() {
        resolver.registerHandler(IllegalArgumentException.class,
                e -> new SimpleCommandExceptionType(new LiteralMessage(e.getMessage())).create());

        CommandSyntaxException res = assertDoesNotThrow(
                () -> resolver.resolve(new RuntimeException("outer", new IllegalArgumentException("inner")))
        );

        assertNotNull(res);
        assertEquals("inner", res.getRawMessage().getString());
    }

    @Test
    void testResolveWrappedCausePassesCauseToHandles() {
        resolver.registerHandler(new CommandExceptionHandler<IllegalArgumentException>() {
            @Override
            public Class<IllegalArgumentException> exceptionType() {
                return IllegalArgumentException.class;
            }

            @Override
            public CommandSyntaxException handle(IllegalArgumentException exception) {
                return new SimpleCommandExceptionType(new LiteralMessage(exception.getMessage())).create();
            }

            @Override
            public boolean handles(IllegalArgumentException exception) {
                return "inner".equals(exception.getMessage());
            }
        });

        CommandSyntaxException res = assertDoesNotThrow(
                () -> resolver.resolve(new RuntimeException("outer", new IllegalArgumentException("inner")))
        );

        assertNotNull(res);
        assertEquals("inner", res.getRawMessage().getString());
    }
}
