package io.github.seal139.jSwarm;

public class Int32 extends PType {
	
	Int32() {
		super(DataType.INT);
	}
	
	Int32(String expression){
		super(DataType.INT, expression);
	}
	
	static Int32 ofExpression(String expression) {
		Int32 target = new Int32(expression);
		
		return target;
	}
	
	public static Int32 ofValue(PType t) {
		Int32 target = new Int32();
		
		KernelWritter writter = target.writter;
		
		// int ${name} = (int)(${expression});
		writter.write(target.getType().getCType() + " " + target.asVariable() + //
						" = (" + target.getType().getCType() + ")(" + t.asExpression() + ");\n");
		
		return target;
	}
	
	public static Int32 ofValue(int i) {
		Int32 target = new Int32();
		
		KernelWritter writter = target.writter;
		
		// int ${name} = i;
		writter.write(target.getType().getCType() + " " + target.asVariable()  + " = " + String.valueOf(i) + ";\n");
		
		
		return target;
	}

}
