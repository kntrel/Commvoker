package com.kntrel.mc.commvoker.command;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Internal API. Public for framework wiring, but not part of the supported external API.
 */
public class CommandPattern implements Iterable<CommandPatternToken> {

    private final CommandPatternToken[] tokens_;
    private final int wildcardIndex_, preWildcardArgs_, postWildcardArgs_;

    public CommandPattern(CommandPatternToken[] tokens) {
        if (tokens.length < 1) {
            throw new IllegalArgumentException("Empty tokens array is illegal");
        }

        int wildcardIndex = -1;
        int preWildcard = 0, postWildcard = 0;
        LinkedList<CommandPatternToken> tokenList = new LinkedList<>();
        for (int i = tokens.length - 1; i >= 0; i--) {
            CommandPatternToken t = tokens[i];
            if (t == null) { t = CommandPatternToken.argument(); }

            if (t.isWildcard()) {
                if (wildcardIndex < 0) {
                    wildcardIndex = i;
                    postWildcard = preWildcard;
                    preWildcard = 0;
                } else {
                    t = CommandPatternToken.argument();
                }
            }

            if (t.isArgument()) {
                preWildcard++;
            }

            tokenList.addFirst(t);
        }
        if (wildcardIndex < 0) {
            wildcardIndex = tokenList.size();
            tokenList.addLast(CommandPatternToken.wildcard());
        }

        this.wildcardIndex_ = wildcardIndex;
        this.preWildcardArgs_ = preWildcard;
        this.postWildcardArgs_ = postWildcard;
        this.tokens_ = tokenList.toArray(new CommandPatternToken[0]);

    }
    public CommandPattern(Collection<CommandPatternToken> tokens) {
        this(tokens.toArray(new CommandPatternToken[0]));
    }


    public int size() {
        return this.tokens_.length;
    }
    public CommandPatternToken getTokenAt(int index) {
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
    public int getWildcardIndex() {
        return this.wildcardIndex_;
    }
    public boolean isWildCardAt(int index) {
        return index == this.wildcardIndex_;
    }
    public int beforeWildcardTokenCount() {
        return this.wildcardIndex_;
    }
    public int afterWildcardTokenCount() {
        return this.tokens_.length - this.wildcardIndex_ - 1;
    }
    public int beforeWildcardArgumentCount() {
        return this.preWildcardArgs_;
    }
    public int afterWildcardArgumentCount() {
        return this.postWildcardArgs_;
    }
    public int argumentCount() {
        return this.preWildcardArgs_ + this.postWildcardArgs_;
    }
    public CommandPatternToken[] beforeWildcardTokens() {
        return Arrays.copyOf(this.tokens_, this.wildcardIndex_);
    }
    public CommandPatternToken[] afterWildcardTokens() {
        int size = this.afterWildcardArgumentCount();
        if (size < 1) {
            return new CommandPatternToken[0];
        }
        CommandPatternToken[] out = new CommandPatternToken[size];
        System.arraycopy(this.tokens_, this.wildcardIndex_ + 1, out, 0, size);
        return out;
    }
    public CommandPatternToken[] beforeWildcardArguments() {
        return this.extractArguments(0, this.wildcardIndex_, this.preWildcardArgs_);
    }
    public CommandPatternToken[] afterWildcardArguments() {
        return this.extractArguments(this.wildcardIndex_ + 1, this.tokens_.length, this.postWildcardArgs_);
    }
    public CommandPatternToken[] arguments() {
        return this.extractArguments(0, this.tokens_.length, this.argumentCount());
    }


    public CommandPatternToken[] toTokensArray() {
        return Arrays.copyOf(this.tokens_, this.tokens_.length);
    }


    @Override public String toString() {
        return Arrays.stream(this.tokens_)
                .map(CommandPatternToken::toString)
                .collect(Collectors.joining(" "));
    }

    @Override public boolean equals(Object o) {
        if (o == null) { return false; }
        if (o == this) { return true; }
        if (!(o instanceof CommandPattern other)) { return false; }
        return Arrays.equals(this.tokens_, other.tokens_);
    }

    @Override
    public Iterator<CommandPatternToken> iterator() {
        return new InnerIterator(this);
    }



    //HELPERS
    private CommandPatternToken[] extractArguments(int from, int to, int size) {
        CommandPatternToken[] out = new CommandPatternToken[size];
        if (out.length < 1) { return out; }

        int outI = 0;
        for (int i = from; i < to; i++) {
            if (outI >= size) { break; }
            CommandPatternToken t = this.tokens_[i];
            if (t.isArgument()) {
                out[outI++] = t;
            }
        }

        return out;
    }

    private static class InnerIterator implements Iterator<CommandPatternToken> {
        private final CommandPattern owner_;
        private int pos_;

        private InnerIterator(CommandPattern owner) {
            this.owner_ = owner;
            this.pos_ = 0;
        }

        @Override
        public boolean hasNext() {
            return this.pos_ < this.owner_.tokens_.length;
        }

        @Override
        public CommandPatternToken next() {
            if (this.pos_ >= this.owner_.tokens_.length) {
                throw new IndexOutOfBoundsException(this.pos_);
            }
            return this.owner_.tokens_[this.pos_++];
        }
    }
}
