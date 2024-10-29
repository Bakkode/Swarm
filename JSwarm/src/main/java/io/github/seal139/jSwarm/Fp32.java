package io.github.seal139.jSwarm;

public class Fp32 extends PType {
	
	Fp32() {
		super(DataType.FLOAT);
	}
	
	Fp32(String expression){
		super(DataType.FLOAT, expression);
	}
	
	static Fp32 ofExpression(String expression) {
		Fp32 target = new Fp32(expression);
		
		return target;
	}
	
	public static Fp32 ofValue(PType t) {
		Fp32 target = new Fp32();
		
		KernelWritter writter = target.writter;
		
		// int ${name} = (int)(${expression});
		writter.write(target.getType().getCType() + " " + target.asVariable() + //
						" = (" + target.getType().getCType() + ")(" + t.asExpression() + ");\n");
		
		return target;
	}
	
	public static Fp32 ofValue(int i) {
		Fp32 target = new Fp32();
		
		KernelWritter writter = target.writter;
		
		// int ${name} = i;
		writter.write(target.getType().getCType() + " " + target.asVariable()  + " = " + String.valueOf(i) + ";\n");
		
		
		return target;
	}

}
