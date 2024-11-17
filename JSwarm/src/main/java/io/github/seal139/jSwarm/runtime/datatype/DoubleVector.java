package io.github.seal139.jSwarm.runtime.datatype;

public class DoubleVector extends Vector<Double> {

	
	public DoubleVector(Purpose purpose) {
		super(purpose, 512, DataType.DOUBLE);
	}
	
	public DoubleVector(Purpose purpose, int bufferSize) {
		super(purpose, bufferSize, DataType.DOUBLE);
	}

}
