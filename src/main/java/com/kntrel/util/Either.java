package com.kntrel.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Either<A, B> {

    public static <A, B> Either<A, B> ofTheOne(A subject) {
        return new Either<>(subject, null);
    }
    public static <A, B> Either<A, B> ofTheOther(B subject) {
        return new Either<>(null, subject);
    }


    private final A theOne_;
    private final B theOther_;

    private Either(A theOne, B theOther) {
        this.theOne_ = theOne;
        this.theOther_ = theOther;
    }


    public Optional<A> theOne() {
        return Optional.ofNullable(this.theOne_);
    }
    public Optional<B> theOther() {
        return Optional.ofNullable(this.theOther_);
    }
    public A getTheOne() {
        return this.theOne_;
    }
    public B getTheOther() {
        return this.theOther_;
    }
    public Object getEither() {
        if (this.theOne_ != null) { return this.theOne_; }
        return this.theOther_;
    }
    public A getTheOneOrThrow(Supplier<? extends RuntimeException> throwable) throws RuntimeException {
        if (this.theOne_ == null) {
            throw throwable.get();
        }
        return this.theOne_;
    }
    public B getTheOtherOrThrow(Supplier<? extends RuntimeException> throwable) throws RuntimeException {
        if (this.theOther_ == null) {
            throw throwable.get();
        }
        return this.theOther_;
    }
    public A getTheOneOrThrow() throws Throwable {
        return this.getTheOneOrThrow(NullPointerException::new);
    }
    public B getTheOtherOrThrow() throws Throwable {
        return this.getTheOtherOrThrow(NullPointerException::new);
    }
    public boolean isTheOne() {
        return this.theOne_ != null;
    }
    public boolean isTheOther() {
        return this.theOther_ != null;
    }
    public void ifIsTheOne(Consumer<A> action) {
        if (this.theOne_ == null) { return; }
        action.accept(this.theOne_);
    }
    public void ifIsTheOther(Consumer<B> action) {
        if (this.theOther_ == null) { return; }
        action.accept(this.theOther_);
    }
    public void ifIsTheOneOrTheOther(Consumer<A> ifTheOne, Consumer<B> ifTheOther) {
        this.ifIsTheOne(ifTheOne); this.ifIsTheOther(ifTheOther);
    }
    public <C> Either<C, B> mapTheOne(Function<A, C> mapper) {
        if (this.theOne_ == null) {
            return new Either<>(null, this.theOther_);
        }
        return new Either<>(mapper.apply(theOne_), null);
    }
    public <C> Either<A, C> mapTheOther(Function<B, C> mapper) {
        if (this.theOther_ == null) {
            return new Either<>(this.theOne_, null);
        }
        return new Either<>(null, mapper.apply(this.theOther_));
    }
    public <C, D> Either<C, D> map(Function<A, C> theOneMapper, Function<B, D> theOtherMapper) {
        if (this.theOne_ == null) {
            return new Either<>(null, theOtherMapper.apply(this.theOther_));
        }
        return new Either<>(theOneMapper.apply(this.theOne_), null);
    }
    public <C> C fold(Function<A, C> theOneMapper, Function<B, C> theOtherMapper) {
        if (this.theOne_ == null) {
            return theOtherMapper.apply(this.theOther_);
        }
        return theOneMapper.apply(this.theOne_);
    }
}
