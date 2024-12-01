package io.github.seal139.jSwarm.runtime;

public final class ExampleKernel extends Program {
	
	public void vecAdd(@Global
						float a[], 
					   @Global float b[],
					   @Global float c[], 
					   @Shared(size = 10) float tmp, 
					   @Global int n) {
		
		int id = getGlobalId(0);
		
		if(id < n) {
			c[id] = a[id] + b[id];
		}
	}
	
	protected final void vecSub(float[] a, float[] b, float c[], int n) {
		int id = getGlobalId(0);
		
		if(id < n) {
			c[id] = a[id] - b[id];
		}
	}
}

