package com.kntrel.mc.commvoker.argument.binding;

import com.mojang.brigadier.arguments.ArgumentType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public sealed abstract class CommandTemplate<S> {

    //FACTORY
    public static <S> Ongoing<S> beginLiteral(String label) {
        var root = new Literal<S>(label);
        return new Fluent<>(root);
    }
    public static <S> Ongoing<S> beginArgument(String label, ArgumentType<?> type) {
        var root = new Argument<S>(label, type);
        return new Fluent<>(root);
    }


    @Override public abstract CommandTemplate<S> clone();


    public static final class Forward<S> extends CommandTemplate<S> {

        //FIELDS
        private String forward_;
        private int occurrence_;

        //CONSTRUCTOR
        private Forward(String forward, int occurrence) { this.forward_ = forward; this.occurrence_ = occurrence; }
        private Forward(String forward) { this(forward, 0); }

        //SETTERS
        public void reforward(String newForward) { this.forward_ = newForward; }

        //GETTERS
        public String forwardsTo() { return this.forward_; }
        public int occurrence() { return this.occurrence_; }
        @Override public Forward<S> clone() { return new Forward<>(this.forward_); }
    }

    public static sealed abstract class Node<S> extends CommandTemplate<S> permits Literal, Argument {

        //FIELDS
        private String label_;
        private Predicate<S> requirement_;
        protected final List<CommandTemplate<S>> children_ = new ArrayList<>();

        //CONSTRUCTOR
        private Node(String label) { this.label_ = label; }

        //SETTERS
        public void rename(String newLabel) { this.label_ = newLabel; }
        public void addChild(CommandTemplate<S> child) { this.children_.add(child); }

        //GETTERS
        public String label() { return this.label_; }
        public Predicate<S> requirement() { return this.requirement_; }
        public List<CommandTemplate<S>> children() { return this.children_; }
        void setRequirement(Predicate<S> requirement) { this.requirement_ = requirement; }
        @Override public abstract Node<S> clone();
    }

    public static final class Literal<S> extends Node<S> {
        //CONSTRUCTOR
        private Literal(String label) { super(label); }

        @Override public Literal<S> clone() {
            Literal<S> clone = new Literal<>(this.label());
            for (CommandTemplate<S> c : this.children_) { clone.addChild(c.clone()); }
            return clone;
        }
    }

    public static final class Argument<S> extends Node<S> {

        //FIELDS
        private final ArgumentType<?> arg_;

        //CONSTRUCTOR
        private Argument(String label, ArgumentType<?> argumentType) {
            super(label); this.arg_ = argumentType;
        }

        //GETTERS
        public ArgumentType<?> argumentType() { return this.arg_; }
        @Override public Argument<S> clone() {
            Argument<S> clone = new Argument<>(this.label(), this.arg_);
            for (CommandTemplate<S> c : this.children_) { clone.addChild(c.clone()); }
            return clone;
        }
    }


    //FLUENT CHAIN
    public interface Ongoing<S> {
        Ongoing<S> literal(String label);
        Ongoing<S> argument(String label, ArgumentType<?> argumentType);
        Ongoing<S> requires(Predicate<S> requirement);
        Terminated<S> then(String label, int occurrence);
        Terminated<S> then(String label);
        Terminated<S> split(CommandTemplate<S>... branches);
        Node<S> end();
    }

    public interface Terminated<S> {
        Node<S> end();
    }

    private static final class Fluent<S> implements Ongoing<S>, Terminated<S>  {
        private final Node<S> root_;
        private Node<S> cursor_;
        private Fluent(Node<S> root) { this.root_ = root; this.cursor_ = root; }

        @Override public Ongoing<S> literal(String label) {
            var lit = new Literal<S>(label);
            this.cursor_.children_.add(lit);
            this.cursor_ = lit;
            return this; // keep returning the same fluent root
        }

        @Override public Ongoing<S> argument(String label, ArgumentType<?> argType) {
            var arg = new Argument<S>(label, argType);
            this.cursor_.children_.add(arg);
            this.cursor_ = arg;
            return this;
        }

        @Override public Terminated<S> split(CommandTemplate<S>... branches) {
            this.cursor_.children_.addAll(Arrays.asList(branches));
            return this;
        }

        @Override public Ongoing<S> requires(Predicate<S> requirement) {
            this.cursor_.setRequirement(requirement);
            return this;
        }

        @Override public Terminated<S> then(String label, int occurrence) {
            this.cursor_.children_.add(new Forward<S>(label, occurrence));
            return this;
        }
        @Override public Terminated<S> then(String label) {
            return this.then(label, 0);
        }

        @Override public Node<S> end() { return root_; }
    }
}