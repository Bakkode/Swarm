package io.github.seal139.jSwarm;

public abstract class Kernel {
	
	public static final class Statement {
		private final Runnable body;
		private final PType condition;
		
		private Statement(Runnable body, PType condition) {
			this.body	   = body;
			this.condition = condition;
		}
		
		public static Statement byCondition(PType condition, Runnable body) {
			return new Statement(body, condition);
		}
		
		public static Statement of(Runnable body) {
			return new Statement(body, null);
		}
	}
	
	//Arithmetic
	protected abstract PType pow (PType v1, PType v2);
	protected abstract PType mul (PType v1, PType v2);
	protected abstract PType div (PType v1, PType v2);
	protected abstract PType mod (PType v1, PType v2);
	protected abstract PType add (PType v1, PType v2);
	protected abstract PType sub (PType v1, PType v2);
	
	protected abstract PType pow (PType v1, Number v2);
	protected abstract PType mul (PType v1, Number v2);
	protected abstract PType div (PType v1, Number v2);
	protected abstract PType mod (PType v1, Number v2);
	protected abstract PType add (PType v1, Number v2);
	protected abstract PType sub (PType v1, Number v2);
	
	protected abstract PType pow (Number v1, PType v2);
	protected abstract PType mul (Number v1, PType v2);
	protected abstract PType div (Number v1, PType v2);
	protected abstract PType mod (Number v1, PType v2);
	protected abstract PType add (Number v1, PType v2);
	protected abstract PType sub (Number v1, PType v2);
	
	protected abstract PType pow (Number v1, Number v2);
	protected abstract PType mul (Number v1, Number v2);
	protected abstract PType div (Number v1, Number v2);
	protected abstract PType mod (Number v1, Number v2);
	protected abstract PType add (Number v1, Number v2);
	protected abstract PType sub (Number v1, Number v2);
	
	//Logical
	protected abstract Int32 equals		(PType v1, PType v2);
	protected abstract Int32 notEquals	(PType v1, PType v2);
	protected abstract PType and		(PType v1, PType v2);
	protected abstract PType nand		(PType v1, PType v2);
	protected abstract PType or			(PType v1, PType v2);
	protected abstract PType nor		(PType v1, PType v2);
	protected abstract PType xor		(PType v1, PType v2);
	protected abstract PType nxor		(PType v1, PType v2);
	protected abstract PType not		(PType v);
	
	protected abstract Int32 equals		(PType v1, Number v2);
	protected abstract Int32 notEquals	(PType v1, Number v2);
	protected abstract PType and		(PType v1, Number v2);
	protected abstract PType nand		(PType v1, Number v2);
	protected abstract PType or			(PType v1, Number v2);
	protected abstract PType nor		(PType v1, Number v2);
	protected abstract PType xor		(PType v1, Number v2);
	protected abstract PType nxor		(PType v1, Number v2);
	protected abstract PType not		(Number v);
	
	protected abstract Int32 equals		(Number v1, PType v2);
	protected abstract Int32 notEquals	(Number v1, PType v2);
	protected abstract PType and		(Number v1, PType v2);
	protected abstract PType nand		(Number v1, PType v2);
	protected abstract PType or			(Number v1, PType v2);
	protected abstract PType nor		(Number v1, PType v2);
	protected abstract PType xor		(Number v1, PType v2);
	protected abstract PType nxor		(Number v1, PType v2);
	
	protected abstract Int32 equals		(Number v1, Number v2);
	protected abstract Int32 notEquals	(Number v1, Number v2);
	protected abstract PType and		(Number v1, Number v2);
	protected abstract PType nand		(Number v1, Number v2);
	protected abstract PType or			(Number v1, Number v2);
	protected abstract PType nor		(Number v1, Number v2);
	protected abstract PType xor		(Number v1, Number v2);
	protected abstract PType nxor		(Number v1, Number v2);
	
	protected abstract Int32 lessThan				(PType v1, PType v2);
	protected abstract Int32 lessThanOrEquals		(PType v1, PType v2);
	protected abstract Int32 greaterThan			(PType v1, PType v2);
	protected abstract Int32 greaterThanOrEquals	(PType v1, PType v2);
	
	protected abstract Int32 lessThan				(Number v1, PType v2);
	protected abstract Int32 lessThanOrEquals		(Number v1, PType v2);
	protected abstract Int32 greaterThan			(Number v1, PType v2);
	protected abstract Int32 greaterThanOrEquals	(Number v1, PType v2);
	
	protected abstract Int32 lessThan				(PType v1, Number v2);
	protected abstract Int32 lessThanOrEquals		(PType v1, Number v2);
	protected abstract Int32 greaterThan			(PType v1, Number v2);
	protected abstract Int32 greaterThanOrEquals	(PType v1, Number v2);
	
	protected abstract Int32 lessThan				(Number v1, Number v2);
	protected abstract Int32 lessThanOrEquals		(Number v1, Number v2);
	protected abstract Int32 greaterThan			(Number v1, Number v2);
	protected abstract Int32 greaterThanOrEquals	(Number v1, Number v2);
	
	// Array
	protected abstract PType initArray(PType size);
	
	// Statement
	protected abstract void execReturn();
	protected abstract void execBreak();
	
	
	protected abstract void execIf(Statement ... statement);
	protected abstract void execWhile(Statement statement);
	protected abstract void execFor(PType initialization, Statement statement, PType postStatement);
	
	void isEven() {
		Int32 i = Int32.ofValue(0);
		
		Int32 v0 = Int32.ofValue(0);
		Int32 v2 = Int32.ofValue(2);
		
		execFor(
			i, 
			Statement.byCondition(lessThan(i, 100), () ->{
				execIf(
						Statement.byCondition(notEquals(0, mod(i, 2)), () ->
							 System.out.println("Odd")
						),
						Statement.byCondition(equals(0, mod(i, 2)), () -> 
							System.out.println("Even")
						),
						
						Statement.of(() -> 
							System.out.println("Undefined")
						)
				);
				
				execReturn();
			})
			, add(i, 1)
		);
	}
	
}

