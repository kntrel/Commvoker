package com.kntrel.mc.commvoker.provided.assemblers;

import com.kntrel.mc.commvoker.argument.binding.CommandTemplate;
import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CollectionAssemblerTest {

    private static List<String> collectArgumentLabels(CommandTemplate<?> t) {
        List<String> labels = new ArrayList<>();
        Deque<CommandTemplate.Element<?>> st = new ArrayDeque<>(t.trees());
        while (!st.isEmpty()) {
            CommandTemplate.Element<?> e = st.pop();
            if (e instanceof CommandTemplate.Node<?> n) {
                if (n instanceof CommandTemplate.Argument<?>) labels.add(n.label());
                n.children().forEach(st::push);
            }
        }
        return labels;
    }

    private static boolean containsLiteral(CommandTemplate<?> t, String literal) {
        Deque<CommandTemplate.Element<?>> st = new ArrayDeque<>(t.trees());
        while (!st.isEmpty()) {
            CommandTemplate.Element<?> e = st.pop();
            if (e instanceof CommandTemplate.Node<?> n) {
                if (n instanceof CommandTemplate.Literal<?> && n.label().equals(literal)) return true;
                n.children().forEach(st::push);
            }
        }
        return false;
    }

    private static <S> ExecutionContext<S> context(Object... kv) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return new ExecutionContext<>(null, m, List.of(), Map.of());
    }

    /* ========================================================================== */
    /* ============================ RELAXED (GREEDY) ============================ */
    /* ========================================================================== */

    @Nested
    class RelaxedMode {

        @Test
        void greedy_onlyOne_max1() {
            var el = StringAssembler.word();
            var col = CollectionAssembler.relaxedListOf(el, 1);

            CommandTemplate<Object> tpl = col.argumentTemplate();
            // No "and"/"only"/"none" in relaxed max==1
            assertFalse(containsLiteral(tpl, "and"));
            assertFalse(containsLiteral(tpl, "only"));
            assertFalse(containsLiteral(tpl, "none"));

            // Arguments are renamed with index 0
            var args = collectArgumentLabels(tpl);
            assertTrue(args.contains("arg0"), "expected v0 in greedy max=1 template");

            // Contextualize one element
            var out = col.contextualize(context("arg0", "A"));
            assertEquals(List.of("A"), out);
        }

        @Test
        void greedy_moreThanTwo_allows_earlyEnds_and_optionalAnd() {
            var el = StringAssembler.word();
            var col = CollectionAssembler.relaxedListOf(el, 1, 3);

            CommandTemplate<Object> tpl = col.argumentTemplate();
            assertTrue(containsLiteral(tpl, "and"));

            // All three indices should be present (v0, v1, v2). Greedy keeps exits at each element.
            var args = collectArgumentLabels(tpl);
            assertTrue(args.containsAll(List.of("arg0", "arg1", "arg2")));

            // Simulate user typed: v0 (end early) -> only v0 participates
            var early = col.contextualize(context("arg0", "A"));
            assertEquals(List.of("A"), early);

            // Simulate "... and <last>" i.e., v0 and v2
            var andLast = col.contextualize(context("arg0", "A", "arg2", "C"));
            assertEquals(List.of("A", "C"), andLast);

            // Simulate v0 v1 (no last) — greedy mode collects in index order
            var twoPlain = col.contextualize(context("arg0", "A", "arg1", "B"));
            assertEquals(List.of("A", "B"), twoPlain);
        }

        @Test
        void greedy_min0_doesNotCreate_none_route_but_allows_one_or_more() {
            var el = StringAssembler.word();
            var col = CollectionAssembler.relaxedListOf(el, /*min*/0, /*max*/3);

            CommandTemplate<Object> tpl = col.argumentTemplate();
            // Optional 'and' exists
            assertTrue(containsLiteral(tpl, "and"));

            // With no components provided, contextualizer yields empty list (it will scan and find nothing)
            var none = col.contextualize(context());
            assertEquals(List.of(), none);

            // With one or more values present, it collects them
            var one = col.contextualize(context("arg0", "A"));
            assertEquals(List.of("A"), one);

            var many = col.contextualize(context("arg0", "A", "arg1", "B", "arg2", "C"));
            assertEquals(List.of("A", "B", "C"), many);
        }
    }

    /* ========================================================================== */
    /* ============================== STRICT MODE =============================== */
    /* ========================================================================== */

    @Nested
    class StrictMode {

        @Test
        void strict_max1_singleElement_no_only_no_and() {
            var el = StringAssembler.word();
            var col = CollectionAssembler.listOf(el, /*max*/1); // min defaults to 1

            CommandTemplate<Object> tpl = col.argumentTemplate();
            assertFalse(containsLiteral(tpl, "and"));
            assertFalse(containsLiteral(tpl, "only"));
            assertFalse(containsLiteral(tpl, "none"));

            var args = collectArgumentLabels(tpl);
            assertEquals(List.of("arg0"), args);

            var out = col.contextualize(context("arg0", "A"));
            assertEquals(List.of("A"), out);
        }

        @Test
        void strict_min1_has_only_and_requires_and_for_multi() {
            var el = StringAssembler.word();
            var col = CollectionAssembler.listOf(el, /*min*/1, /*max*/3);

            CommandTemplate<Object> tpl = col.argumentTemplate();
            assertTrue(containsLiteral(tpl, "only"), "strict(min=1) should expose 'only' branch");
            assertTrue(containsLiteral(tpl, "and"), "strict(min=1) should require 'and' to finish multi-item");

            // Contextualization order still index-based:
            // emulate "only v0"
            var only = col.contextualize(context("arg0", "A"));
            assertEquals(List.of("A"), only);

            // emulate "v0 and v2" (multi via 'and last')
            var multi = col.contextualize(context("arg0", "A", "arg2", "C"));
            assertEquals(List.of("A", "C"), multi);

            // If user provided v0 v1 (no last), contextualizer still sees 2 items,
            // but the syntax wouldn't parse without 'and' — this checks runtime mapping only:
            var rawTwo = col.contextualize(context("arg0", "A", "arg1", "B"));
            assertEquals(List.of("A", "B"), rawTwo);
        }

        @Test
        void strict_min0_has_none_and_only_and_and() {
            var el = StringAssembler.word();
            var col = CollectionAssembler.listOf(el, /*min*/0, /*max*/3);

            CommandTemplate<Object> tpl = col.argumentTemplate();
            assertTrue(containsLiteral(tpl, "none"), "strict(min=0) must expose 'none'");
            assertTrue(containsLiteral(tpl, "only"), "strict(min=0) should expose 'only'");
            assertTrue(containsLiteral(tpl, "and"),  "strict(min=0) should expose 'and' for multi");

            // none → empty
            var none = col.contextualize(context());
            assertEquals(List.of(), none);

            // only
            var only = col.contextualize(context("arg0", "A"));
            assertEquals(List.of("A"), only);

            // v0 and v2
            var many = col.contextualize(context("arg0", "A", "arg2", "C"));
            assertEquals(List.of("A", "C"), many);
        }

        @Test
        void strict_moreThanTwo_min2_requires_two_before_and() {
            var el = StringAssembler.word();
            var col = CollectionAssembler.listOf(el, /*min*/2, /*max*/4);

            CommandTemplate<Object> tpl = col.argumentTemplate();
            assertTrue(containsLiteral(tpl, "and"));

            // Structure sanity: arguments v0..v3 exist
            var args = collectArgumentLabels(tpl);
            assertTrue(args.containsAll(List.of("arg0","arg1","arg2","arg3")));

            // Contextualizer order with 'and last' (v0, v3)
            var ctx = col.contextualize(context("arg0", "A", "arg3", "D"));
            assertEquals(List.of("A","D"), ctx);

            // Single element present: runtime gives 1, but parse would be rejected (no 'only' with min=2)
            var single = col.contextualize(context("arg0", "A"));
            assertEquals(List.of("A"), single);
        }
    }

    /* ========================================================================== */
    /* ================================ Edges =================================== */
    /* ========================================================================== */

    @Test
    void renaming_applies_per_index_and_keeps_literals() {
        var el = StringAssembler.word();
        var relaxed = CollectionAssembler.relaxedListOf(el, /*min*/1, /*max*/3);
        CommandTemplate<Object> tpl = relaxed.argumentTemplate();

        // Literals are not renamed; arguments v0..v2 appear
        var args = collectArgumentLabels(tpl);
        assertTrue(args.containsAll(List.of("arg0","arg1","arg2")));
        assertTrue(containsLiteral(tpl, "and"));
        assertFalse(containsLiteral(tpl, "only"));
        assertFalse(containsLiteral(tpl, "none"));
    }

    @Test
    void contextualize_preserves_index_order_even_with_last_shortcut() {
        var el = StringAssembler.word();
        var greedy = CollectionAssembler.relaxedListOf(el, /*min*/1, /*max*/4);

        // Provide v0, v3 (emulates "... and <last>")
        var out = greedy.contextualize(context("arg0","A","arg3","D"));
        assertEquals(List.of("A","D"), out);

        // Provide v1, v3 (skips v0)
        var out2 = greedy.contextualize(context("arg1","B","arg3","D"));
        assertEquals(List.of("B","D"), out2);
    }
}