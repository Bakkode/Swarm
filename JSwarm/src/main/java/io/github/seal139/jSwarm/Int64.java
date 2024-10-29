package io.github.seal139.jSwarm;

public class Int64 extends PType {
	
	Int64() {
		super(DataType.LONG);
	}
	
	Int64(String expression){
		super(DataType.LONG, expression);
	}
	
	static Int64 ofExpression(String expression) {
		Int64 target = new Int64(expression);
		
		return target;
	}
	
	public static Int64 ofValue(PType t) {
		Int64 target = new Int64();
		
		KernelWritter writter = target.writter;
		
		// int ${name} = (int)(${expression});
		writter.write(target.getType().getCType() + " " + target.asVariable() + //
						" = (" + target.getType().getCType() + ")(" + t.asExpression() + ");\n");
		
		return target;
	}
	
	public static Int64 ofValue(int i) {
		Int64 target = new Int64();
		
		KernelWritter writter = target.writter;
		
		// int ${name} = i;
		writter.write(target.getType().getCType() + " " + target.asVariable()  + " = " + String.valueOf(i) + ";\n");
		
		
		return target;
	}

}
