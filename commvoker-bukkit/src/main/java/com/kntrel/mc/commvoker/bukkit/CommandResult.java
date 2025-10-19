package com.kntrel.mc.commvoker.bukkit;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;

public class CommandResult {

    //FACTORY
    public static CommandResult success(Message message) {
        return new CommandResult(true, message);
    }
    public static CommandResult fail(Message message) {
        return new CommandResult(false, message);
    }
    public static CommandResult success(String message) {
        return success(new LiteralMessage(message));
    }
    public static CommandResult fail(String message) {
        return fail(new LiteralMessage(message));
    }


    //FIELDS
    private final Message message_;
    private final boolean success_;


    //CONSTRUCTORS
    public CommandResult(boolean success, Message message) {
        this.success_ = success;
        this.message_ = message;
    }


    //GETTERS
    public Message getMessage() {
        return this.message_;
    }
    public boolean wasSuccessful() {
        return this.success_;
    }
}
