package com.kntrel.mc.commvoker.argument.context;

import com.kntrel.mc.commvoker.argument.binder.ArgumentBinder;
import com.kntrel.mc.commvoker.argument.binding.ArgumentBinding;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.TransformAssembler;
import com.kntrel.mc.commvoker.command.Command;
import com.kntrel.mc.commvoker.mock.MockCommvoker;
import com.kntrel.mc.commvoker.provided.assemblers.IntegerAssembler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class ExecutionContextTest {

    //ASSETS
    private static final Object SRC = new Object();
    private @interface ToBag {}
    private static class ToBagAssembler implements TransformAssembler<Object, Integer, Integer> {

        @Override
        public Assembler<? super Object, ? extends Integer> delegate() {
            return IntegerAssembler.integer();
        }

        @Override
        public Integer compose(ExecutionContext<?> ctx, Integer object) {
            ctx.bag().put("num", object);
            return object;
        }
    }
    private static final ArgumentBinding<Object, ?, ExecutionContext<?>> EXECUTION_BINDING = ArgumentBinder
            .implicit(Function.identity())
            .toClass((Class<ExecutionContext<?>>) (Class<?>) ExecutionContext.class)
            .bind();
    private static final ArgumentBinding<Object, ?, Integer> TO_BAG_BINDING = ArgumentBinder
            .argumentAssembler(ToBagAssembler::new)
            .toAnnotation(ToBag.class)
            .toClass(Integer.class)
            .bind();

    public static final class Holder {
        ExecutionContext<?> ctx;

        @Command("test")
        public void test(ExecutionContext<?> ctx) {
            this.ctx = ctx;
        }

        @Command("test2 {str}")
        public void test(String prev, ExecutionContext<?> ctx) {
            this.ctx = ctx;
        }

        @Command("test3 {str} {num}")
        public void test(String prev, @ToBag int num, ExecutionContext<?> ctx) {
            this.ctx = ctx;
            this.ctx.bag().put("num", num);
        }
    }



    //FIELDS
    private final Holder holder;
    private MockCommvoker commvoker;



    //SETUP
    public ExecutionContextTest() {
        this.holder = new Holder();
    }
    @BeforeEach
    void setup() {
        this.commvoker = new MockCommvoker();
        this.commvoker.getArgumentRegistry().register(EXECUTION_BINDING, TO_BAG_BINDING);
        this.commvoker.register(this.holder);
    }


    //TESTS
    @Test void noArgs() {
        assertExecutes("test");
        assertNotNull(this.holder.ctx);
        assertSame(SRC, this.holder.ctx.source());
        assertFalse(this.holder.ctx.hasPreviousArguments());
        assertTrue(this.holder.ctx.bag().isEmpty());
    }

    @Test void previousArg() {
        assertExecutes("test2 hello");
        assertNotNull(this.holder.ctx);
        assertSame(SRC, this.holder.ctx.source());
        assertTrue(this.holder.ctx.hasPreviousArguments());
        assertEquals(1, this.holder.ctx.previousArgumentsCount());
        assertEquals("hello", this.holder.ctx.previousArgument());
        assertSame(this.holder.ctx.previousArgument(), this.holder.ctx.previousArgument(0));
        assertTrue(this.holder.ctx.bag().isEmpty());
    }

    @Test void baggedArg() {
        assertExecutes("test3 hello 42");
        assertNotNull(this.holder.ctx);
        assertSame(SRC, this.holder.ctx.source());
        assertTrue(this.holder.ctx.hasPreviousArguments());
        assertEquals(2, this.holder.ctx.previousArgumentsCount());
        assertEquals("hello", this.holder.ctx.previousArgument(1));
        assertEquals(42, this.holder.ctx.previousArgument(0));
        assertTrue(this.holder.ctx.hasBagObject("num"));
        assertEquals(42, this.holder.ctx.bagObject("num", Integer.class));
    }


    //PRIVATE
    private void assertExecutes(String string) {
        assertDoesNotThrow(() -> this.commvoker.execute(string, SRC));
    }
}
