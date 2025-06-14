package io.github.seal139.jSwarm.runtime;

public class NativeException extends RuntimeException {

    private static final long serialVersionUID = 1236290368163221322L;

    public NativeException() {
        fillInStackTrace();
    }

    @Override
    public String getMessage() { return "Cannot load native library"; }

}