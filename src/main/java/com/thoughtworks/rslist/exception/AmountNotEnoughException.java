package com.thoughtworks.rslist.exception;

public class AmountNotEnoughException extends RuntimeException {
    private String error;

    public AmountNotEnoughException(String error) {
        this.error = error;
    }

    @Override
    public String getMessage() {
        return error;
    }
}
