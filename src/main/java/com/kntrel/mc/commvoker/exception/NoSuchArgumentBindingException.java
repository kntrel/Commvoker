package com.kntrel.mc.commvoker.exception;

import com.kntrel.mc.commvoker.argument.ArgumentContext;

import java.lang.reflect.Parameter;
import java.util.Arrays;

public class NoSuchArgumentBindingException extends ArgumentResolutionException {

    private final ArgumentContext argumentContext_;

    public NoSuchArgumentBindingException(ArgumentContext argumentContext) {
        this.argumentContext_ = argumentContext;
    }
    public NoSuchArgumentBindingException(ArgumentContext argumentContext, String msg) {
        super(msg);
        this.argumentContext_ = argumentContext;
    }

    @Override public String getMessage() {
        String msg = super.getMessage();
        if (msg != null && !msg.isEmpty()) { return msg; }
        Parameter param = this.argumentContext_.parameter();
        StringBuilder m = new StringBuilder();

        m.append("No registered ArgumentBuilder able to handle argument: ");

        Arrays.stream(param.getAnnotations())
                .forEach(a -> m.append('@').append(a.getClass().getName()).append(' '));

        m       .append(param.getParameterizedType().getTypeName())
                .append(' ').append(param.getName())
                .append(" in method ")
                .append(this.argumentContext_.method().getDeclaringClass().getName())
                .append('#')
                .append(this.argumentContext_.method().getName());

        return m.toString();
    }

}
