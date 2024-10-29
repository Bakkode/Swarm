package io.github.seal139.jSwarm;

public class PType {
	static enum DataType {
		SHORT("short"),
		INT("int"),
		LONG("long long int"),
		FLOAT("float"),
		DOUBLE("double");
		
		private final String cType;

	    // Constructor to initialize parameters
		DataType(String cType) {
			this.cType = cType;
	    }

	    public String getCType() {
	        return cType;
	    }
	}
	
	private long handle;
	private final DataType t;
	private String expression;
	
	protected final KernelWritter writter = KernelWritter.getWritter();
	
	PType(DataType dt) {
		this.t = dt;
		this.expression = null;
	}
	
	PType(DataType dt, String expression){
		this.t = dt;
		this.expression = expression;
	}
	
	final DataType getType(){
		return t;
	}
	
	/**
	 * Change current variable's value from another variable's value. <br/>
	 * This method provide auto casting from another data type
	 * @param from
	 */
	public void set(PType from) throws Exception {
		writter.write(this.asVariable()  + " = (" + getType().getCType() + ")(" + from.asExpression()  + ");\n");
	}
	
	String asVariable() {
		return "v" + String.valueOf(handle);
	}
	
	String asExpression() {
		if(expression != null) {
			writter.write(getType().getCType() + " " + asVariable() + //
						" = (" + getType().getCType() + ")(" + expression + ");\n");

			expression = null;
		}
		
		return asVariable();
	}
	
	String forStatement() {
		return expression == null ? asVariable() : expression;
	}
}
