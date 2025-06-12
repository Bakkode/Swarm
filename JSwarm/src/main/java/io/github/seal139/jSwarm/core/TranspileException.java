package io.github.seal139.jSwarm.core;

public class TranspileException extends RuntimeException {

    private static final long serialVersionUID = 1236290368163221322L;

    private final String e;

    public TranspileException(String message) {
        this.e = message;

        fillInStackTrace();
    }

    @Override
    public String getMessage() { return this.e; }

}