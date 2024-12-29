package io.github.seal139.jSwarm.runtime.datatype;

public class FloatVector extends Vector<Float> {

    public FloatVector(Purpose purpose) {
        super(purpose, 512, DataType.FLOAT);
    }

    public FloatVector(Purpose purpose, int bufferSize) {
        super(purpose, bufferSize, DataType.FLOAT);
    }

}
