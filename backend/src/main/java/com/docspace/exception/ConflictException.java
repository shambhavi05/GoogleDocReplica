package com.docspace.exception;

public class ConflictException extends RuntimeException {

    private final transient Object latestState;

    public ConflictException(String message, Object latestState) {
        super(message);
        this.latestState = latestState;
    }

    public Object getLatestState() {
        return latestState;
    }
}
