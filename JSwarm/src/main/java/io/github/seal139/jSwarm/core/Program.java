package io.github.seal139.jSwarm.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represent GPGPU Program that contains kernel function
 */
public abstract class Program {
    long[] indexPtr;
    long[] localPtr;

    protected long getCurrentIndex(int dimension) {
        return this.indexPtr[dimension];
    }

    protected long getLocalIndex(int dimension) {
        return this.localPtr[dimension];
    }

    protected void synchronize() {

    }

    protected int getGlobalId() { return 0; }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    protected static @interface Global {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    protected static @interface SharedMemory {
        String name();

        DataType type();

        int size() default 0;
    }

    protected enum DataType {
        SHORT, INTEGER, LONG, FLOAT, DOUBLE
    }
}
