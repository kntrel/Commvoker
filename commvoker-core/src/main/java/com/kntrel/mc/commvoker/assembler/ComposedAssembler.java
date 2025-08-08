package com.kntrel.mc.commvoker.assembler;

import com.kntrel.util.tuple.Pair;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.*;
import java.util.stream.Collectors;

public non-sealed interface ComposedAssembler<S, T> extends Assembler<S, T> {

    List<Pair<Assembler<? super S, ?>, SuggestionProvider<? super S>>> delegates();

    T compose(CommandContext<? extends S> ctx, Object[] objects);

    default T contextualize(CommandContext<? extends S> ctx, Object[] objects) {

        // A frame for "where we are" in the DFS
        final class Frame {
            final ComposedAssembler<? super S, ?> node;
            final java.util.List<Assembler<? super S, ?>> delegates;
            final Object[] upstream;
            int i = 0;

            Frame(ComposedAssembler<? super S, ?> node) {
                this.node = node;
                this.delegates = node.delegates().stream()
                        .map(Pair::first)
                        .collect(Collectors.toCollection(ArrayList::new));
                this.upstream = new Object[delegates.size()];
            }
        }

        int in = 0;
        Deque<Frame> stack = new ArrayDeque<>();
        stack.addLast(new Frame(this));
        Object rootValue = null;

        while (!stack.isEmpty()) {
            Frame f = stack.peekLast();

            if (f.i < f.delegates.size()) {
                Assembler<? super S, ?> delegate = f.delegates.get(f.i);

                if (delegate instanceof EndAssembler<?>) {
                    if (in >= objects.length) {
                        throw new IllegalStateException("Not enough input objects for leaves.");
                    }
                    f.upstream[f.i++] = objects[in++];
                } else {
                    f.i++; // reserve this slot; when child returns we’ll fill upstream[i-1]
                    stack.addLast(new Frame((ComposedAssembler<? super S, ?>) delegate));
                }

                continue;
            }

            Object val = f.node.compose(ctx, f.upstream);
            stack.pollLast();
            if (stack.isEmpty()) {
                rootValue = val; // this was the root
            } else {
                Frame parent = stack.peekLast();
                // parent.i already advanced past this child; write into its slot
                parent.upstream[parent.i - 1] = val;
            }
        }

        return (T) rootValue;
    }


    @Override
    default boolean isImplicit() {
        for (var d : this.delegates()) {
            if (!d.first().isImplicit()) { return false; }
        }
        return true;
    }
}
