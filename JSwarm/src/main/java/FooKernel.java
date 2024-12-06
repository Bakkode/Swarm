

public final class FooKernel extends Program {
	
	public final void vecMull(float[] a, float[] b, float c[], int n) {
		int id = getGlobalId(0);
		
		if(id < n) {
			c[id] = a[id] * b[id];
		}
	}
	
	public final void vecDiv(float[] a, float[] b, float c[], int n) {
		int id = getGlobalId(0);
		
		if(id < n) {
			c[id] = a[id] / b[id];
		}
	}
}

