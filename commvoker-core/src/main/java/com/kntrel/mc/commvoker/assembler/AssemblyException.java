package com.kntrel.mc.commvoker.assembler;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;

import java.util.Optional;

public class AssemblyException extends Exception {

    //ASSETS
    public enum Reference { OBJECT, INDEX, KEY, NONE }


    //FIELDS
    private final Message message;
    private Object component_ = null;
    private int componentIndex_ = -1;
    private String componentKey_ = null;


    //CONSTRUCTORS
    public AssemblyException(String msg) {
        super(msg);
        this.message = new LiteralMessage(msg);
    }
    public AssemblyException(Message msg) {
        super(msg.getString());
        this.message = msg;
    }
    public AssemblyException(String msg, int componentIndex) {
        this(msg);
        this.componentIndex_ = componentIndex;
    }
    public AssemblyException(Message msg, int componentIndex) {
        this(msg);
        this.componentIndex_ = componentIndex;
    }
    public AssemblyException(String msg, String componentKey) {
        this(msg);
        this.componentKey_ = componentKey;
    }
    public AssemblyException(Message msg, String componentKey) {
        this(msg);
        this.componentKey_ = componentKey;
    }
    public AssemblyException(String msg, Object component) {
        this(msg);
        this.component_ = component;
    }
    public AssemblyException(Message msg, Object component) {
        this(msg);
        this.component_ = component;
    }


    //GETTERS
    public Message getDynamicMessage() {
        return this.message;
    }
    public Optional<Object> component() {
        return Optional.ofNullable(this.component_);
    }
    public Optional<Integer> componentIndex() {
        return (this.componentIndex_ >= 0) ? Optional.of(this.componentIndex_) : Optional.empty();
    }
    public Optional<String> componentKey() {
        return Optional.ofNullable(this.componentKey_);
    }
    public Reference referenceType() {
        if (this.component_ != null) {
            return Reference.OBJECT;
        } else if (this.componentIndex_ >= 0) {
            return Reference.INDEX;
        } else if (this.componentKey_ != null) {
            return Reference.KEY;
        } else {
            return Reference.NONE;
        }
    }
}
