package io.github.seal139.jSwarm.runtime.datatype;

public class IntegerVector extends Vector<Integer> {

	
	public IntegerVector(Purpose purpose) {
		super(purpose, 512, DataType.INTEGER);
	}
	
	public IntegerVector(Purpose purpose, int bufferSize) {
		super(purpose, bufferSize, DataType.INTEGER);
	}

}
