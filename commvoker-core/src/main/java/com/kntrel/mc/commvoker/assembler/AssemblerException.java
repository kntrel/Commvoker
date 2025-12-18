package com.kntrel.mc.commvoker.assembler;

import com.kntrel.mc.commvoker.argument.Component;
import com.kntrel.mc.commvoker.argument.context.ExecutionContext;

import java.util.List;
import java.util.Optional;

public class AssemblerException extends RuntimeException {

    //FACTORY
    static AssemblerException from(AssemblyException cause, Assembler<?, ?> assembler, ExecutionContext<?> context) {

        List<? extends Component<?>> components = context.components();
        Component<?> component = switch (cause.referenceType()) {
            case OBJECT -> components.stream()
                    .filter(c -> c.value().equals(cause.component().orElse(null)))
                    .findFirst()
                    .orElse(null);
            case INDEX -> {
                int index = cause.componentIndex().orElse(-1);
                if (index < 0 || index >= components.size()) {
                    yield null;
                }
                yield components.get(index);
            }
            case KEY -> components.stream()
                    .filter(c -> c.key().equals(cause.componentKey().orElse(null)))
                    .findFirst()
                    .orElse(null);
            default -> null;
        };

        return new AssemblerException(cause, assembler, component, context);
    }


    //FIELDS
    private final Assembler<?, ?> assembler_;
    private final Component<?> component_;
    private final ExecutionContext<?> context_;

    //CONSTRUCTORS
    public AssemblerException(AssemblyException cause, Assembler<?, ?> assembler, Component<?> component, ExecutionContext<?> context) {
        super(cause);
        this.assembler_ = assembler;
        this.component_ = component;
        this.context_ = context;
    }

    //GETTERS
    public Assembler<?, ?> getAssembler() {
        return this.assembler_;
    }
    public Component<?> getComponent() {
        return this.component_;
    }
    public ExecutionContext<?> getContext() {
        return this.context_;
    }
    @Override public AssemblyException getCause() {
        return (AssemblyException) super.getCause();
    }
}
