package io.github.seal139.jSwarm.runtime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public abstract class Program {

	@Retention(RetentionPolicy.RUNTIME) // The annotation will be available at runtime
	@Target({ElementType.PARAMETER})
	protected static @interface Global {
		
	}
	
	@Retention(RetentionPolicy.RUNTIME) // The annotation will be available at runtime
	@Target({ElementType.PARAMETER})
	protected static @interface Shared {
		int size() default 1;
	}
	
	protected int getGlobalId(int i) {
		return 0;
	}
}
