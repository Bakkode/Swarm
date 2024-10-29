package io.github.seal139.jSwarm;

public class Fp64 extends PType {
	
	Fp64() {
		super(DataType.DOUBLE);
	}
	
	Fp64(String expression){
		super(DataType.DOUBLE, expression);
	}
	
	static Fp64 ofExpression(String expression) {
		Fp64 target = new Fp64(expression);
		
		return target;
	}
	
	public static Fp64 ofValue(PType t) {
		Fp64 target = new Fp64();
		
		KernelWritter writter = target.writter;
		
		// int ${name} = (int)(${expression});
		writter.write(target.getType().getCType() + " " + target.asVariable() + //
						" = (" + target.getType().getCType() + ")(" + t.asExpression() + ");\n");
		
		return target;
	}
	
	public static Fp64 ofValue(int i) {
		Fp64 target = new Fp64();
		
		KernelWritter writter = target.writter;
		
		// int ${name} = i;
		writter.write(target.getType().getCType() + " " + target.asVariable()  + " = " + String.valueOf(i) + ";\n");
		
		
		return target;
	}

}
