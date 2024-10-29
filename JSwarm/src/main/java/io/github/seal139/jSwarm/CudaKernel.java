package io.github.seal139.jSwarm;

import io.github.seal139.jSwarm.PType.DataType;

public class CudaKernel extends Kernel {
	
	protected final KernelWritter writter = KernelWritter.getWritter();
	
	private DataType getType(PType v1, PType v2) {
		return v1.getType().ordinal() > v2.getType().ordinal() ? v1.getType() : v2.getType();
	}
	
	private DataType extractType(Number t) {
		DataType ordinal;
		
		if(t instanceof Short) {
			ordinal = DataType.SHORT;
		}
		else if(t instanceof Integer) {
			ordinal = DataType.INT;
		}
		else if(t instanceof Long) {
			ordinal = DataType.LONG;
		}
		else if(t instanceof Float) {
			ordinal = DataType.FLOAT;
		}
		else if(t instanceof Double) {
			ordinal = DataType.DOUBLE;
		}
		else {
			ordinal = DataType.SHORT;
		}
		
		return ordinal;
	}
	
	private DataType getType(PType v1, Number v2) {
		DataType ordinal = extractType(v2);
		return v1.getType().ordinal() > ordinal.ordinal() ? v1.getType() : ordinal;
	}
	
	private DataType getType(Number v1, Number v2) {
		DataType ordinal1 = extractType(v1);
		DataType ordinal2 = extractType(v2);
		
		return ordinal1.ordinal() > ordinal2.ordinal() ? ordinal1 : ordinal2;
	}
	
	
	@Override
	protected PType pow(PType v1, PType v2) {
		return new PType(getType(v1, v2), "pow((" + v1.forStatement() + "), (" + v2.forStatement() + "))");
	}

	@Override
	protected PType mul(PType v1, PType v2) {
		return new PType(getType(v1, v2), "((" + v1.forStatement() + ") * (" + v2.forStatement() + "))");
	}

	@Override
	protected PType div(PType v1, PType v2) {
		return new PType(getType(v1, v2), "((" + v1.forStatement() + ") / (" + v2.forStatement() + "))");
	}

	@Override
	protected PType mod(PType v1, PType v2) {
		return new PType(getType(v1, v2), "((" + v1.forStatement() + ") % (" + v2.forStatement() + "))");
	}

	@Override
	protected PType add(PType v1, PType v2) {
		return new PType(getType(v1, v2), "((" + v1.forStatement() + ") + (" + v2.forStatement() + "))");
	}

	@Override
	protected PType sub(PType v1, PType v2) {
		return new PType(getType(v1, v2), "((" + v1.forStatement() + ") - (" + v2.forStatement() + "))");
	}

	@Override
	protected PType pow(PType v1, Number v2) {
		return new PType(getType(v1, v2), "pow((" + v1.forStatement() + "), (" + v2.toString() + "))");
	}

	@Override
	protected PType mul(PType v1, Number v2) {
		return new PType(getType(v1, v2), "((" + v1.forStatement() + ") * (" + v2.toString() + "))");
	}

	@Override
	protected PType div(PType v1, Number v2) {
		return new PType(getType(v1, v2), "((" + v1.forStatement() + ") / (" + v2.toString() + "))");
	}

	@Override
	protected PType mod(PType v1, Number v2) {
		return new PType(getType(v1, v2), "((" + v1.forStatement() + ") % (" + v2.toString() + "))");
	}

	@Override
	protected PType add(PType v1, Number v2) {
		return new PType(getType(v1, v2), "((" + v1.forStatement() + ") + (" + v2.toString() + "))");
	}

	@Override
	protected PType sub(PType v1, Number v2) {
		return new PType(getType(v1, v2), "((" + v1.forStatement() + ") - (" + v2.toString() + "))");
	}

	@Override
	protected PType pow(Number v1, PType v2) {
		return new PType(getType(v2, v1), "pow((" + v1.toString() + "), (" + v2.forStatement() + "))");
	}

	@Override
	protected PType mul(Number v1, PType v2) {
		return new PType(getType(v2, v1), "((" + v1.toString() + ") * (" + v2.forStatement() + "))");
	}

	@Override
	protected PType div(Number v1, PType v2) {
		return new PType(getType(v2, v1), "((" + v1.toString() + ") / (" + v2.forStatement() + "))");
	}

	@Override
	protected PType mod(Number v1, PType v2) {
		return new PType(getType(v2, v1), "((" + v1.toString() + ") % (" + v2.forStatement() + "))");
	}

	@Override
	protected PType add(Number v1, PType v2) {
		return new PType(getType(v2, v1), "((" + v1.toString() + ") + (" + v2.forStatement() + "))");
	}

	@Override
	protected PType sub(Number v1, PType v2) {
		return new PType(getType(v2, v1), "((" + v1.toString() + ") - (" + v2.forStatement() + "))");
	}

	@Override
	protected PType pow(Number v1, Number v2) {
		return new PType(getType(v1, v2), "pow((" + v1.toString() + "), (" + v2.toString() + "))");
	}

	@Override
	protected PType mul(Number v1, Number v2) {
		return new PType(getType(v1, v2), "((" + v1.toString() + ") * (" + v2.toString() + "))");
	}

	@Override
	protected PType div(Number v1, Number v2) {
		return new PType(getType(v1, v2), "((" + v1.toString() + ") / (" + v2.toString() + "))");
	}

	@Override
	protected PType mod(Number v1, Number v2) {
		return new PType(getType(v1, v2), "((" + v1.toString() + ") % (" + v2.toString() + "))");
	}

	@Override
	protected PType add(Number v1, Number v2) {
		return new PType(getType(v1, v2), "((" + v1.toString() + ") + (" + v2.toString() + "))");
	}

	@Override
	protected PType sub(Number v1, Number v2) {
		return new PType(getType(v1, v2), "((" + v1.toString() + ") - (" + v2.toString() + "))");
	}

	// ======================
	
	@Override
	protected Int32 equals(PType v1, PType v2) {
		return Int32.ofExpression("((" + v1.forStatement() + ") == (" + v2.forStatement() + "))");
	}

	@Override
	protected Int32 notEquals(PType v1, PType v2) {
		return Int32.ofExpression("((" + v1.forStatement() + ") != (" + v2.forStatement() + "))");
	}

	@Override
	protected PType and(PType v1, PType v2) {
		return new PType(getType(v1, v2), "((" + v1.forStatement() + ") & (" + v2.forStatement() + "))");
	}

	@Override
	protected PType nand(PType v1, PType v2) {
		return new PType(getType(v1, v2), "!((" + v1.forStatement() + ") & (" + v2.forStatement() + "))");
	}

	@Override
	protected PType or(PType v1, PType v2) {
		return new PType(getType(v1, v2), "((" + v1.forStatement() + ") | (" + v2.forStatement() + "))");
	}

	@Override
	protected PType nor(PType v1, PType v2) {
		return new PType(getType(v1, v2), "!((" + v1.forStatement() + ") | (" + v2.forStatement() + "))");
	}

	@Override
	protected PType xor(PType v1, PType v2) {
		return new PType(getType(v1, v2), "((" + v1.forStatement() + ") ^ (" + v2.forStatement() + "))");
	}

	@Override
	protected PType nxor(PType v1, PType v2) {
		return new PType(getType(v1, v2), "!((" + v1.forStatement() + ") ^ (" + v2.forStatement() + "))");
	}

	@Override
	protected PType not(PType v) {
		return new PType(v.getType(), "!(" + v.forStatement() + ")");
	}

	@Override
	protected Int32 equals(PType v1, Number v2) {
		return Int32.ofExpression("((" + v1.forStatement() + ") == (" + v2.toString() + "))");
	}

	@Override
	protected Int32 notEquals(PType v1, Number v2) {
		return Int32.ofExpression("((" + v1.forStatement() + ") != (" + v2.toString() + "))");
	}

	@Override
	protected PType and(PType v1, Number v2) {
		return new PType(getType(v1, v2), "((" + v1.forStatement() + ") & (" + v2.toString() + "))");
	}

	@Override
	protected PType nand(PType v1, Number v2) {
		return new PType(getType(v1, v2), "!((" + v1.forStatement() + ") & (" + v2.toString() + "))");
	}

	@Override
	protected PType or(PType v1, Number v2) {
		return new PType(getType(v1, v2), "((" + v1.forStatement() + ") | (" + v2.toString() + "))");
	}

	@Override
	protected PType nor(PType v1, Number v2) {
		return new PType(getType(v1, v2), "!((" + v1.forStatement() + ") | (" + v2.toString() + "))");
	}

	@Override
	protected PType xor(PType v1, Number v2) {
		return new PType(getType(v1, v2), "((" + v1.forStatement() + ") ^ (" + v2.toString() + "))");
	}

	@Override
	protected PType nxor(PType v1, Number v2) {
		return new PType(getType(v1, v2), "!((" + v1.forStatement() + ") ^ (" + v2.toString() + "))");
	}

	@Override
	protected PType not(Number v) {
		return new PType(extractType(v), "!(" + v.toString() + ")");
	}

	@Override
	protected Int32 equals(Number v1, PType v2) {
		return Int32.ofExpression("((" + v1.toString() + ") == (" + v2.forStatement() + "))");
	}

	@Override
	protected Int32 notEquals(Number v1, PType v2) {
		return Int32.ofExpression("((" + v1.toString() + ") != (" + v2.forStatement() + "))");
	}

	@Override
	protected PType and(Number v1, PType v2) {
		return new PType(getType(v2, v1), "((" + v1.toString() + ") & (" + v2.forStatement() + "))");
	}

	@Override
	protected PType nand(Number v1, PType v2) {
		return new PType(getType(v2, v1), "!((" + v1.toString() + ") & (" + v2.forStatement() + "))");
	}

	@Override
	protected PType or(Number v1, PType v2) {
		return new PType(getType(v2, v1), "((" + v1.toString() + ") | (" + v2.forStatement() + "))");
	}

	@Override
	protected PType nor(Number v1, PType v2) {
		return new PType(getType(v2, v1), "!((" + v1.toString() + ") | (" + v2.forStatement() + "))");
	}

	@Override
	protected PType xor(Number v1, PType v2) {
		return new PType(getType(v2, v1), "((" + v1.toString() + ") ^ (" + v2.forStatement() + "))");
	}

	@Override
	protected PType nxor(Number v1, PType v2) {
		return new PType(getType(v2, v1), "!((" + v1.toString() + ") ^ (" + v2.forStatement() + "))");
	}

	@Override
	protected Int32 equals(Number v1, Number v2) {
		return Int32.ofExpression("((" + v1.toString() + ") == (" + v2.toString() + "))");
	}

	@Override
	protected Int32 notEquals(Number v1, Number v2) {
		return Int32.ofExpression("((" + v1.toString() + ") != (" + v2.toString() + "))");
	}

	@Override
	protected PType and(Number v1, Number v2) {
		return new PType(getType(v2, v1), "((" + v1.toString() + ") & (" + v2.toString() + "))");
	}

	@Override
	protected PType nand(Number v1, Number v2) {
		return new PType(getType(v2, v1), "!((" + v1.toString() + ") & (" + v2.toString() + "))");
	}

	@Override
	protected PType or(Number v1, Number v2) {
		return new PType(getType(v2, v1), "((" + v1.toString() + ") | (" + v2.toString() + "))");
	}

	@Override
	protected PType nor(Number v1, Number v2) {
		return new PType(getType(v2, v1), "!((" + v1.toString() + ") | (" + v2.toString() + "))");
	}

	@Override
	protected PType xor(Number v1, Number v2) {
		return new PType(getType(v2, v1), "((" + v1.toString() + ") ^ (" + v2.toString() + "))");
	}

	@Override
	protected PType nxor(Number v1, Number v2) {
		return new PType(getType(v2, v1), "!((" + v1.toString() + ") ^ (" + v2.toString() + "))");
	}

	// =====================
	
	@Override
	protected Int32 lessThan(PType v1, PType v2) {
		return Int32.ofExpression("!((" + v1.forStatement() + ") < (" + v2.forStatement() + "))");
	}

	@Override
	protected Int32 lessThanOrEquals(PType v1, PType v2) {
		return Int32.ofExpression("!((" + v1.forStatement() + ") <= (" + v2.forStatement() + "))");
	}

	@Override
	protected Int32 greaterThan(PType v1, PType v2) {
		return Int32.ofExpression("!((" + v1.forStatement() + ") > (" + v2.forStatement() + "))");
	}

	@Override
	protected Int32 greaterThanOrEquals(PType v1, PType v2) {
		return Int32.ofExpression("!((" + v1.forStatement() + ") >= (" + v2.forStatement() + "))");
	}

	@Override
	protected Int32 lessThan(Number v1, PType v2) {
		return Int32.ofExpression("!((" + v1.toString() + ") < (" + v2.forStatement() + "))");
	}

	@Override
	protected Int32 lessThanOrEquals(Number v1, PType v2) {
		return Int32.ofExpression("!((" + v1.toString() + ") <= (" + v2.forStatement() + "))");
	}

	@Override
	protected Int32 greaterThan(Number v1, PType v2) {
		return Int32.ofExpression("!((" + v1.toString() + ") > (" + v2.forStatement() + "))");
	}

	@Override
	protected Int32 greaterThanOrEquals(Number v1, PType v2) {
		return Int32.ofExpression("!((" + v1.toString() + ") >= (" + v2.forStatement() + "))");
	}

	@Override
	protected Int32 lessThan(PType v1, Number v2) {
		return Int32.ofExpression("!((" + v1.forStatement() + ") < (" + v2.toString() + "))");
	}

	@Override
	protected Int32 lessThanOrEquals(PType v1, Number v2) {
		return Int32.ofExpression("!((" + v1.forStatement() + ") <= (" + v2.toString() + "))");
	}

	@Override
	protected Int32 greaterThan(PType v1, Number v2) {
		return Int32.ofExpression("!((" + v1.forStatement() + ") > (" + v2.toString() + "))");
	}

	@Override
	protected Int32 greaterThanOrEquals(PType v1, Number v2) {
		return Int32.ofExpression("!((" + v1.forStatement() + ") >= (" + v2.toString() + "))");
	}

	@Override
	protected Int32 lessThan(Number v1, Number v2) {
		return Int32.ofExpression("!((" + v1.toString() + ") < (" + v2.toString() + "))");
	}

	@Override
	protected Int32 lessThanOrEquals(Number v1, Number v2) {
		return Int32.ofExpression("!((" + v1.toString() + ") <= (" + v2.toString() + "))");
	}

	@Override
	protected Int32 greaterThan(Number v1, Number v2) {
		return Int32.ofExpression("!((" + v1.toString() + ") > (" + v2.toString() + "))");
	}

	@Override
	protected Int32 greaterThanOrEquals(Number v1, Number v2) {
		return Int32.ofExpression("!((" + v1.toString() + ") >= (" + v2.toString() + "))");
	}

	@Override
	protected PType initArray(PType size) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void execReturn() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void execBreak() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void execIf(Statement... statement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void execWhile(Statement statement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void execFor(PType initialization, Statement statement, PType postStatement) {
		// TODO Auto-generated method stub
		
	}
	
	
}
