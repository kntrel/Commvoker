package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.argument.binding.ArgumentBinding;
import com.kntrel.mc.commvoker.callback.ReturnCallback;
import com.kntrel.mc.commvoker.command.Command;
import com.kntrel.mc.commvoker.argument.ArgumentRegistry;
import com.kntrel.mc.commvoker.error.CommandExceptionHandler;
import com.kntrel.mc.commvoker.error.CommandExceptionResolver;
import com.kntrel.mc.commvoker.provided.ArgumentBindings;
import com.kntrel.mc.commvoker.command.CommandPatternToken;
import com.kntrel.mc.commvoker.exception.BadCommandClassException;
import com.kntrel.mc.commvoker.exception.BadCommandMethodException;
import com.kntrel.mc.commvoker.exception.BadCommandTokenException;
import com.kntrel.mc.commvoker.requirement.AnnotatedRequirement;
import com.kntrel.mc.commvoker.requirement.Requirement;
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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class BaseCommvoker<S> {

    //FIELDS
    private final ArgumentResolverImpl<S> argumentResolver_;
    private final CommandExceptionResolver exceptionResolver_;
    private final CommandParser<S> commandParser_;
    private final CommandDispatcher<S> dispatcher_;
    private final Set<Class<?>> instanceClasses_;
    private final Class<S> sourceClass_;
    private final Map<CommandNode<S>, Predicate<S>> requirements_;
    private final CallbackManager<S> callbackManager_;


    //CONSTRUCTORS
    public BaseCommvoker(Class<S> sourceClass, CommandDispatcher<S> commandDispatcher) {
        this.sourceClass_ = sourceClass;
        this.dispatcher_ = commandDispatcher;
        this.argumentResolver_ = new ArgumentResolverImpl<>();
        this.exceptionResolver_ = new CommandExceptionResolverImpl();
        this.commandParser_ = new CommandParser<>(this.argumentResolver_, this.exceptionResolver_);
        this.instanceClasses_ = new HashSet<>();
        this.requirements_ = new IdentityHashMap<>();
        this.callbackManager_ = new CallbackManager<>();

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
            this.callbackManager_.addInvoker(parsed.invoker());

            List<RequirementBridge<S>> annotated;
            try {
                annotated = this.extractRequirements(m);
            } catch (BadCommandMethodException e) {
                throw new BadCommandClassException(src, e);
            }
            List<Predicate<S>> reqs = Stream.concat(
                    parsed.requirements().stream(),
                    annotated.stream()
            ).toList();
            Predicate<S> req;
            if (reqs.isEmpty()) {
                req = RequirementNode.always();
            } else if (reqs.size() < 2) {
                req = reqs.getFirst();
            } else {
                req = Multipredicate.and(reqs);
            }
            this.applyRequirement(commandTree, req);
        }

        this.instanceClasses_.add(src.getClass());
    }
    public void register(LiteralArgumentBuilder<S> tree) {
        this.dispatcher_.register(tree);
    }
    public int execute(String command, S src) throws CommandSyntaxException {
        return this.dispatcher_.execute(command, src);
    }


    //CONFIG
    public void registerArgument(ArgumentBinding<? super S, ?, ?> descriptor) {
        this.argumentResolver_.register(descriptor);
    }
    public void registerExceptionHandler(CommandExceptionHandler<?> handler) {
        this.exceptionResolver_.registerHandler(handler);
    }
    public <E extends Throwable> CommandExceptionHandler<E> registerExceptionHandler(Class<E> exceptionType, Function<E, CommandSyntaxException> handler) {
        return this.exceptionResolver_.registerHandler(exceptionType, handler);
    }
    public void registerCallback(ReturnCallback<? super S, ?> callback) {
        this.callbackManager_.addCallback(callback);
    }
    @SuppressWarnings("rawtypes")
    public void removeCallback(Class<? extends ReturnCallback> callbackClass) {
        this.callbackManager_.removeCallback(callbackClass);
    }


    //GETTERS
    public ArgumentRegistry<S> getArgumentRegistry() {
        return this.argumentResolver_;
    }
    protected CommandDispatcher<S> getCommandDispatcher() {
        return this.dispatcher_;
    }
    public CommandExceptionResolver getExceptionResolver() {
        return this.exceptionResolver_;
    }


    //HELPERS
    private List<RequirementBridge<S>> extractRequirements(Method method) throws BadCommandMethodException {
        List<RequirementBridge<S>> out = new ArrayList<>();
        for (var a : method.getAnnotations()) {
            Requires requires = (a instanceof Requires r) ? r : a.annotationType().getAnnotation(Requires.class);
            if (requires == null) { continue; }

            for (Class<? extends AnnotatedRequirement<?, ?>> reqClass : requires.value()) {
                AnnotatedRequirement<?, ?> reqInstance = annotatedRequirementInstance(reqClass);
                Annotation annotation = (reqInstance instanceof Requirement<?>) ? requires : a;

                RequirementBridge<S> bridge;
                try {
                    bridge = new RequirementBridge<>(this.sourceClass_, reqInstance, annotation);
                } catch (IllegalArgumentException e) {
                    throw new BadCommandMethodException(method, e.getMessage(), e);
                }

                out.add(bridge);
            }
        }
        return out;
    }
    private static AnnotatedRequirement<?, ?> annotatedRequirementInstance(Class<? extends AnnotatedRequirement<?, ?>> requirementClass) {
        try {
            var constructor = requirementClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Could not instantiate requirement class '" + requirementClass.getName() + "'", e);
        }
    }
    private void applyRequirement(LiteralArgumentBuilder<S> tree, Predicate<S> requirement) {
        CommandNode<S> root = this.dispatcher_.getRoot().getChild(tree.getLiteral());
        if (root == null) { return; }

        Deque<Pair<CommandNode<S>, CommandNode<S>>> stack = new ArrayDeque<>();
        stack.push(Pair.of(tree.build(), root));

        while (!stack.isEmpty()) {
            Pair<CommandNode<S>, CommandNode<S>> p = stack.pop();
            CommandNode<S> from = p.first();
            CommandNode<S> to = p.second();

            if (to.getRequirement() instanceof RequirementNode<S> rn) {
                rn.or(requirement);
            }

            Collection<CommandNode<S>> next = from.getChildren();
            if (next.isEmpty()) {
                this.requirements_.put(to, requirement);
            }
            if (!to.getChildren().isEmpty() && to.getCommand() instanceof CommandMethodInvoker<S> cmi) {
                Predicate<S> hardReq = this.requirements_.remove(to);
                if (hardReq != null) {
                    cmi.requires(hardReq);
                }
            }


            next.forEach(c -> {
                CommandNode<S> child = to.getChild(c.getName());
                if (child != null) { stack.push(Pair.of(c, child)); }
            });
        }
    }
}