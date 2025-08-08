package com.kntrel.mc.commvoker.exception;

public class BadCommandTokenException extends Exception {

    private final String command_, token_;
    private String msg_;
    private final int pos_;


    public BadCommandTokenException(String command, int pos, String message) {
        super(message);
        this.command_ = command;
        this.token_ = extractToken(this.command_, pos);
        this.pos_ = pos;
        this.msg_ = null;
    }
    public BadCommandTokenException(String command, int pos) {
        this(command, pos, null);
    }


    private String getCommand() {
        return this.command_;
    }
    public String getToken() {
        return this.token_;
    }
    public int getPosition() {
        return this.pos_;
    }

    @Override public String getMessage() {
        if (this.msg_ != null) {
            return this.msg_;
        }

        StringBuilder msg = new StringBuilder(super.getMessage());
        if (!msg.isEmpty()) {
            msg.append('\n');
        }
        msg.append("> ").append(this.getCommand()).append("\n> ");
        int i = this.getPosition();
        while (i-- > 0) {
            msg.append(' ');
        }

        this.msg_ = msg.toString();
        return this.msg_;
    }


    private static String extractToken(String command, int pos) {
        int s = pos, e = pos, len = command.length();
        while (s >= 0 && command.charAt(s) != ' ') { s--; }
        while (e < len && command.charAt(e) != ' ') { e++; }

        return command.substring(s + 1, e);
    }
}
