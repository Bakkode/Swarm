package io.github.seal139.jSwarm.runtime.datatype;

public class ShortVector extends Vector<Short> {

    public ShortVector(Purpose purpose) {
        super(purpose, 512, DataType.SHORT);
    }

    public ShortVector(Purpose purpose, int bufferSize) {
        super(purpose, bufferSize, DataType.SHORT);
    }

}
