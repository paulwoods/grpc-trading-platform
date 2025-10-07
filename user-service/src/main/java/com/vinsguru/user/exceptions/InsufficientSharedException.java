package com.vinsguru.user.exceptions;

public class InsufficientSharedException extends RuntimeException{

    private static final String MESSAGE = "User [id=%d] does not have enough shares to complete the transaction.";

    public InsufficientSharedException(Integer userId) {
        super(MESSAGE.formatted(userId));
    }
}
