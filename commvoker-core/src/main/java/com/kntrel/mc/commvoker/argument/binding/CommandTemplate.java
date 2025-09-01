package com.kntrel.mc.commvoker.argument.binding;

import com.mojang.brigadier.arguments.ArgumentType;

import java.util.*;
import java.util.function.Predicate;

public class CommandTemplate<S> {

    //FACTORY
    public static <S> Ongoing<S> literal(String label) {
        var root = new Literal<S>(label);
        return new Fluent<>(root);
    }
    public static <S> Ongoing<S> argument(String label, ArgumentType<?> type) {
        var root = new Argument<S>(label, type);
        return new Fluent<>(root);
    }
    public static <S> CommandTemplate<S> split(CommandTemplate.Node<S>... branches) {
        return new CommandTemplate<>(Arrays.asList(branches));
    }
    @SafeVarargs
    public static <S> CommandTemplate<S> merge(CommandTemplate<S>... templates) {
        CommandTemplate<S> tmp = new CommandTemplate<>(Collections.emptyList());
        tmp.trees_ = Arrays.stream(templates)
                .flatMap(t -> t.trees_.stream())
                .toList();
        tmp.leaves_ = Arrays.stream(templates)
                .flatMap(t -> t.leaves_.stream())
                .toList();
        return tmp;
    }
    public static <S> Forward<S> forward(String target) {
        return new Forward<>(target);
    }
    public static <S> Exit<S> exitPoint() {
        return Exit.instance();
    }
    public static <S> CommandTemplate<S> empty() {
        return new CommandTemplate<>(Collections.emptyList());
    }


    //FIELDS
    private Collection<Node<S>> trees_;
    private List<Node<S>> leaves_;


    //CONSTRUCTOR
    private CommandTemplate(Collection<Node<S>> trees) {
        this.trees_ = trees;
        this.leaves_ = this.trees_.stream()
                .flatMap(t -> findExitPoints(t).stream())
                .toList();
    }


    //GETTERS
    public List<CommandTemplate.Node<S>> trees() { return List.copyOf(this.trees_); }
    public List<CommandTemplate.Node<S>> exitPoints() {
        return this.leaves_;
    }


    //UTILITY
    public void append(CommandTemplate<S> next) {
        this.leaves_.forEach(l -> {
            l.children().remove(Exit.instance());
            next.trees_.forEach(l::addChild);
        });
        this.leaves_ = next.leaves_;
    }
    @Override public CommandTemplate<S> clone() {
        return new CommandTemplate<>(this.trees_.stream().map(Node::clone).toList());
    }


    //HELPERS
    private static <S> List<Node<S>> findExitPoints(Node<S> root) {
        Set<Node<S>> out = new HashSet<>();
        Deque<Node<S>> stack = new ArrayDeque<>();
        stack.add(root);

        while (!stack.isEmpty()) {
            Node<S> node = stack.pollLast();
            boolean exitPointed = false;
            for (Element<S> child : node.children()) switch (child) {
                case Node<S> n -> stack.addLast(n);
                case Exit<S> e -> out.add(node);
                default -> {}
            }
        }

        return List.copyOf(out);
    }



    /* --------------------------------------------------- INNER CLASSES ---------------------------------------------------------*/
    public static sealed abstract class Element<S> permits Forward, Node, Exit {
        @Override public abstract Element<S> clone();
    }

    public static final class Forward<S> extends Element<S> {

        //FIELDS
        private String forward_;
        private final int occurrence_;

        //CONSTRUCTOR
        private Forward(String forward, int occurrence) { this.forward_ = forward; this.occurrence_ = occurrence; }
        private Forward(String forward) { this(forward, 0); }

        //SETTERS
        public void reforward(String newForward) { this.forward_ = newForward; }

        //GETTERS
        public String forwardsTo() { return this.forward_; }
        public int occurrence() { return this.occurrence_; }
        @Override public Forward<S> clone() { return new Forward<>(this.forward_, this.occurrence_); }
    }

    public static final class Exit<S> extends Element<S> {

        @SuppressWarnings("rawtypes")
        private static final Exit INSTANCE = new Exit();
        @SuppressWarnings("unchecked")
        private static <S> Exit<S> instance() {
            return INSTANCE;
        }

        private Exit() {}

        public Element<S> clone() {
            return instance();
        }
    }

    public static sealed abstract class Node<S> extends Element<S> permits Literal, Argument {

        //FIELDS
        protected String label_;
        protected Predicate<S> requirement_;
        protected final List<CommandTemplate.Element<S>> children_ = new ArrayList<>();

        //CONSTRUCTOR
        private Node(String label) { this.label_ = label; }

        //SETTERS
        public void rename(String newLabel) { this.label_ = newLabel; }
        public void addChild(CommandTemplate.Element<S> child) { this.children_.add(child); }
        public void setRequirement(Predicate<S> requirement) { this.requirement_ = requirement; }

        //GETTERS
        public String label() { return this.label_; }
        public Predicate<S> requirement() { return this.requirement_; }
        public List<CommandTemplate.Element<S>> children() { return this.children_; }
        @Override public abstract Node<S> clone();
    }

    public static final class Literal<S> extends Node<S> {
        //CONSTRUCTOR
        private Literal(String label) { super(label); }

        @Override public Literal<S> clone() {
            Literal<S> clone = new Literal<>(this.label());
            clone.requirement_ = this.requirement_;
            for (CommandTemplate.Element<S> c : this.children_) { clone.addChild(c.clone()); }
            return clone;
        }
    }

    public static final class Argument<S> extends Node<S> {

        //FIELDS
        private final ArgumentType<?> arg_;
        private Suggester<? extends S> suggester_;

        //CONSTRUCTOR
        private Argument(String label, ArgumentType<?> argumentType) {
            super(label); this.arg_ = argumentType;
            this.suggester_ = null;
        }

        //SETTERS
        public void setSuggestionProvider(Suggester<? extends S> suggester) { this.suggester_ = suggester; }

        //GETTERS
        public ArgumentType<?> argumentType() { return this.arg_; }
        public Suggester<? extends S> suggester() { return this.suggester_; }
        @Override public Argument<S> clone() {
            Argument<S> clone = new Argument<>(this.label(), this.arg_);
            clone.requirement_ = this.requirement_;
            clone.suggester_ = this.suggester_;
            for (CommandTemplate.Element<S> c : this.children_) { clone.addChild(c.clone()); }
            return clone;
        }
    }


    //FLUENT CHAIN
    public interface Ongoing<S> {
        Ongoing<S> literal(String label);
        Ongoing<S> requires(Predicate<S> requirement);
        Ongoing<S> exitPoint();
        OngoingArgument<S> argument(String label, ArgumentType<?> argumentType);
        Terminated<S> then(String label, int occurrence);
        Terminated<S> then(String label);
        Terminated<S> split(CommandTemplate.Element<S>... branches);
        CommandTemplate<S> end();
        Node<S> endBranch();
    }

    public interface OngoingArgument<S> extends Ongoing<S> {
        Ongoing<S> suggests(Suggester<S> suggester);
    }

    public interface Terminated<S> {
        CommandTemplate<S> end();
        Node<S> endBranch();
    }

    private static final class Fluent<S> implements OngoingArgument<S>, Terminated<S>  {
        private final Node<S> root_;
        private Node<S> cursor_;
        private Fluent(Node<S> root) { this.root_ = root; this.cursor_ = root; }

        @Override public Ongoing<S> literal(String label) {
            var lit = new Literal<S>(label);
            this.cursor_.children_.add(lit);
            this.cursor_ = lit;
            return this; // keep returning the same fluent root
        }
        @Override public Ongoing<S> requires(Predicate<S> requirement) {
            this.cursor_.setRequirement(requirement);
            return this;
        }
        @Override public Ongoing<S> exitPoint() {
            Collection<CommandTemplate.Element<S>> children = this.cursor_.children_;
            Exit<S> exit = Exit.instance();
            if (!children.contains(exit)) { children.add(exit); }
            return this;
        }
        @Override public OngoingArgument<S> argument(String label, ArgumentType<?> argType) {
            var arg = new Argument<S>(label, argType);
            this.cursor_.children_.add(arg);
            this.cursor_ = arg;
            return this;
        }

        @Override public Terminated<S> split(CommandTemplate.Element<S>... branches) {
            this.cursor_.children_.addAll(Arrays.asList(branches));
            return this;
        }
        @Override
        public Ongoing<S> suggests(Suggester<S> suggester) {
            if (this.cursor_ instanceof CommandTemplate.Argument<S> arg) {
                arg.setSuggestionProvider(suggester);
            }
            return this;
        }

        @Override public Terminated<S> then(String label, int occurrence) {
            this.cursor_.children_.add(new Forward<S>(label, occurrence));
            return this;
        }
        @Override public Terminated<S> then(String label) {
            return this.then(label, 0);
        }

        @Override public CommandTemplate<S> end() {
            this.exitPoint();
            return new CommandTemplate<>(List.of(this.root_));
        }

        @Override public Node<S> endBranch() {
            return this.root_;
        }
    }
}