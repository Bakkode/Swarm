package io.github.seal139.jSwarm.runtime.datatype;

public class LongVector extends Vector<Long> {

    public LongVector(Purpose purpose) {
        super(purpose, 512, DataType.LONG);
    }

    public LongVector(Purpose purpose, int bufferSize) {
        super(purpose, bufferSize, DataType.LONG);
    }

}
