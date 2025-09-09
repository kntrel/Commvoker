package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.command.Command;
import com.kntrel.mc.commvoker.argument.ArgumentRegistry;
import com.kntrel.mc.commvoker.provided.ArgumentBindings;
import com.kntrel.mc.commvoker.command.CommandPatternToken;
import com.kntrel.mc.commvoker.exception.BadCommandClassException;
import com.kntrel.mc.commvoker.exception.BadCommandMethodException;
import com.kntrel.mc.commvoker.exception.BadCommandTokenException;
import com.kntrel.mc.commvoker.requirement.AnnotatedRequirement;
import com.kntrel.mc.commvoker.requirement.Requires;
import com.kntrel.util.Multipredicate;
import com.kntrel.util.tuple.Pair;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class BaseCommvoker<S> {

    //FIELDS
    private final ArgumentResolverImpl<S> argumentResolver_;
    private final CommandParser<S> commandParser_;
    private final CommandDispatcher<S> dispatcher_;
    private final Set<Class<?>> instanceClasses_;
    private final Class<S> sourceClass_;


    //CONSTRUCTORS
    public BaseCommvoker(Class<S> sourceClass, CommandDispatcher<S> commandDispatcher) {
        this.sourceClass_ = sourceClass;
        this.dispatcher_ = commandDispatcher;
        this.argumentResolver_ = new ArgumentResolverImpl<>();
        this.commandParser_ = new CommandParser<>(this.argumentResolver_);
        this.instanceClasses_ = new HashSet<>();

        ArgumentBindings.all().forEach(this.argumentResolver_::register);
    }


    //UTILITY
    public void register(Object src) {
        if (this.instanceClasses_.contains(src.getClass())) {
            throw new IllegalStateException("An instance of '" + src.getClass().getName() + "' has already been registered into this Commvoker");
        }

        Class<?> clazz = src.getClass();
        List<Method> commandMethods = Arrays.stream(clazz.getMethods())
                .filter(m -> m.isAnnotationPresent(Command.class))
                .toList();

        Command outerAnnotation = clazz.getAnnotation(Command.class);
        boolean nested = outerAnnotation != null;
        CommandPatternToken[] outerTokens = new CommandPatternToken[0];

        if (nested) try {
            if (outerAnnotation.value().isEmpty()) {
                outerTokens = this.commandParser_.tokenize(Utils.toSnakeCase(clazz.getSimpleName()));
            } else {
                outerTokens = this.commandParser_.tokenize(outerAnnotation.value());
                if (outerTokens.length < 1 || !outerTokens[0].isLiteral()) {
                    outerTokens = Utils.arrayJoin(this.commandParser_.tokenize(Utils.toSnakeCase(clazz.getSimpleName())), outerTokens);
                }
            }
        } catch (BadCommandTokenException e) {
            throw new BadCommandClassException(src, e);
        }

        for (Method m : commandMethods) {
            Command annotation = m.getAnnotation(Command.class);
            String raw = annotation.value();
            CommandPatternToken[] tokens;
            try {
                tokens = this.commandParser_.tokenize(raw);
            } catch (BadCommandTokenException e) {
                throw new BadCommandClassException(src, e);
            }
            if (tokens.length < 1 || !tokens[0].isLiteral()) {
                if (annotation.extend() && !nested) {
                    throw new BadCommandClassException(src, new BadCommandMethodException(m, "'extend = true' command methods are only valid inside @Command annotated classes"));
                }
                if (!annotation.extend()) {
                    tokens = Utils.arrayJoin(
                            new CommandPatternToken[] { CommandPatternToken.literal(Utils.toSnakeCase(m.getName())) },
                            tokens
                    );
                }
            }
            if (nested) {
                tokens = Utils.arrayJoin(outerTokens, tokens);
            }

            CommandParser.Result<S> parsed;
            try {
                parsed = this.commandParser_.brigadierCommand(tokens, m, src);
            } catch (BadCommandMethodException e) {
                throw new BadCommandClassException(src, e);
            }

            LiteralArgumentBuilder<S> commandTree = parsed.tree();
            this.register(commandTree);
            List<Predicate<S>> reqs = Stream.concat(
                    parsed.requirements().stream(),
                    this.extractRequirements(m).stream()
            ).toList();
            this.applyRequirement(commandTree, reqs.isEmpty() ? RequirementNode.always() : Multipredicate.and(reqs));
        }

        this.instanceClasses_.add(src.getClass());
    }

    public void register(LiteralArgumentBuilder<S> tree) {
        this.dispatcher_.register(tree);
    }

    public int execute(String command, S src) throws CommandSyntaxException {
        return this.dispatcher_.execute(command, src);
    }


    //GETTERS
    public ArgumentRegistry<S> getArgumentRegistry() {
        return this.argumentResolver_;
    }

    protected CommandDispatcher<S> getCommandDispatcher() {
        return this.dispatcher_;
    }


    //HELPERS
    private List<RequirementBridge<S>> extractRequirements(Method method) {
        List<RequirementBridge<S>> out = new ArrayList<>();
        for (var a : method.getAnnotations()) {
            Requires requires = (a instanceof Requires r) ? r : a.annotationType().getAnnotation(Requires.class);
            if (requires == null) { continue; }

            for (Class<? extends AnnotatedRequirement<?, ?>> reqClass : requires.value()) {
                AnnotatedRequirement<?, ?> reqInstance = annotatedRequirementInstance(reqClass);
                out.add(new RequirementBridge<>(this.sourceClass_, reqInstance, requires));
            }
        }
        return out;
    }
    private static AnnotatedRequirement<?, ?> annotatedRequirementInstance(Class<? extends AnnotatedRequirement<?, ?>> requirementClass) {
        try {
            return requirementClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Could not instantiate requirement class '" + requirementClass.getName() + "'", e);
        }
    }
    private void applyRequirement(LiteralArgumentBuilder<S> tree, Predicate<S> requirement) {
        CommandNode<S> root = this.dispatcher_.getRoot().getChild(tree.getLiteral());
        if (root == null) { return; }

        Deque<Pair<CommandNode<S>, CommandNode<S>>> stack = new ArrayDeque<>();
        tree.getArguments().forEach(c -> {
            CommandNode<S> child = root.getChild(c.getName());
            if (child != null) { stack.push(Pair.of(c, child)); }
        });

        while (!stack.isEmpty()) {
            Pair<CommandNode<S>, CommandNode<S>> p = stack.pop();
            CommandNode<S> from = p.first();
            CommandNode<S> to = p.second();

            Predicate<S> req = to.getRequirement();
            if (req instanceof RequirementNode<S> rn) {
                rn.or(requirement);
            }

            from.getChildren().forEach(c -> {
                CommandNode<S> child = to.getChild(c.getName());
                if (child != null) { stack.push(Pair.of(c, child)); }
            });
        }
    }
}