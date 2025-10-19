package com.kntrel.mc.commvoker.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

public class CommandDefinition implements Iterable<CommandToken> {

    private final CommandToken[] tokens_;
    private final int literalCount_;

    public CommandDefinition(CommandToken[] tokens) {
        if (tokens.length < 1) {
            throw new IllegalArgumentException("Empty tokens array is illegal");
        }
        this.tokens_ = Arrays.copyOf(tokens, tokens.length);
        int literalCount = 0;
        for (CommandToken t : this.tokens_) {
            if (t.isLiteral()) { literalCount++; }
        }
        this.literalCount_ = literalCount;
    }
    public CommandDefinition(Collection<CommandToken> tokens) {
        this(tokens.toArray(new CommandToken[0]));
    }


    public int tokenCount() {
        return this.tokens_.length;
    }
    public int literalCount() {
        return this.literalCount_;
    }
    public int argumentCount() {
        return this.tokenCount() - this.literalCount();
    }
    public CommandToken getTokenAt(int index) {
        return this.tokens_[index];
    }
    public String getWordAt(int index) {
        return this.getTokenAt(index).toString();
    }
    public String getLabelAt(int index) {
        return this.getTokenAt(index).label();
    }
    public boolean isArgumentAt(int index) {
        return this.getTokenAt(index).isArgument();
    }
    public boolean isLiteralAt(int index) {
        return this.getTokenAt(index).isLiteral();
    }
    public CommandToken[] toTokensArray() {
        return Arrays.copyOf(this.tokens_, this.tokens_.length);
    }


    @Override public String toString() {
        return Arrays.stream(this.tokens_)
                .map(CommandToken::toString)
                .collect(Collectors.joining(" "));
    }

    @Override public boolean equals(Object o) {
        if (o == null) { return false; }
        if (o == this) { return true; }
        if (!(o instanceof CommandDefinition other)) { return false; }
        return Arrays.equals(this.tokens_, other.tokens_);
    }

    @Override
    public Iterator<CommandToken> iterator() {
        return new InnerIterator(this);
    }


    private static class InnerIterator implements Iterator<CommandToken> {
        private final CommandDefinition owner_;
        private int pos_;

        private InnerIterator(CommandDefinition owner) {
            this.owner_ = owner;
            this.pos_ = 0;
        }

        @Override
        public boolean hasNext() {
            return this.pos_ < this.owner_.tokens_.length;
        }

        @Override
        public CommandToken next() {
            if (this.pos_ >= this.owner_.tokens_.length) {
                throw new IndexOutOfBoundsException(this.pos_);
            }
            return this.owner_.tokens_[this.pos_++];
        }
    }
}
