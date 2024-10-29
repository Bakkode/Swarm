package io.github.seal139.jSwarm;

public class Int16 extends PType {
	
	Int16() {
		super(DataType.SHORT);
	}
	
	Int16(String expression){
		super(DataType.SHORT, expression);
	}
	
	static Int16 ofExpression(String expression) {
		Int16 target = new Int16(expression);
		
		return target;
	}
	
	public static Int16 ofValue(PType t) {
		Int16 target = new Int16();
		
		KernelWritter writter = target.writter;
		
		// int ${name} = (int)(${expression});
		writter.write(target.getType().getCType() + " " + target.asVariable() + //
						" = (" + target.getType().getCType() + ")(" + t.asExpression() + ");\n");
		
		return target;
	}
	
	public static Int16 ofValue(int i) {
		Int16 target = new Int16();
		
		KernelWritter writter = target.writter;
		
		// int ${name} = i;
		writter.write(target.getType().getCType() + " " + target.asVariable()  + " = " + String.valueOf(i) + ";\n");
		
		
		return target;
	}

}
