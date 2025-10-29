package com.kntrel.mc.commvoker.bukkit;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class CommandResult {

    //FACTORY
    public static CommandResult success() {
        return new CommandResult(true, null);
    }
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
    private final Map<Player, Message> playerMessages_;


    //CONSTRUCTORS
    public CommandResult(boolean success, Message message) {
        this.success_ = success;
        this.message_ = message;
        this.playerMessages_ = new HashMap<>();
    }


    //GETTERS
    public Message getMessage() {
        return this.message_;
    }
    public boolean wasSuccessful() {
        return this.success_;
    }
    public Map<Player, Message> getPlayerMessages() {
        return this.playerMessages_;
    }


    //SETTERS
    public void setPlayerMessages(Map<Player, Message> playerMessages) {
        this.playerMessages_.clear();
        this.playerMessages_.putAll(playerMessages);
    }
    public void setPlayerMessageStrings(Map<Player, String> playerMessages) {
        this.playerMessages_.clear();
        playerMessages.forEach((p, s) -> this.playerMessages_.put(p, new LiteralMessage(s)));
    }
    public void messageToPlayer(Player player, Message message) {
        this.playerMessages_.put(player, message);
    }
    public void messageToPlayer(Player player, String message) {
        this.playerMessages_.put(player, new LiteralMessage(message));
    }
}
