package com.kntrel.mc.commvoker.exception;

import com.kntrel.mc.commvoker.argument.ParameterContext;

import java.lang.reflect.Parameter;
import java.util.Arrays;

public class NoSuchArgumentBindingException extends ArgumentResolutionException {

    private final ParameterContext parameterContext_;

    public NoSuchArgumentBindingException(ParameterContext parameterContext) {
        this.parameterContext_ = parameterContext;
    }
    public NoSuchArgumentBindingException(ParameterContext parameterContext, String msg) {
        super(msg);
        this.parameterContext_ = parameterContext;
    }

    @Override public String getMessage() {
        String msg = super.getMessage();
        if (msg != null && !msg.isEmpty()) { return msg; }
        Parameter param = this.parameterContext_.parameter();
        StringBuilder m = new StringBuilder();

        m.append("No registered ArgumentBuilder able to handle argument: ");

        Arrays.stream(param.getAnnotations())
                .forEach(a -> m.append('@').append(a.getClass().getName()).append(' '));

        m       .append(param.getParameterizedType().getTypeName())
                .append(' ').append(param.getName())
                .append(" in method ")
                .append(this.parameterContext_.method().getDeclaringClass().getName())
                .append('#')
                .append(this.parameterContext_.method().getName());

        return m.toString();
    }

}
