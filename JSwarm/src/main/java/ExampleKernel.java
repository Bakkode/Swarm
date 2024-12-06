

public final class ExampleKernel extends Program {
	
	public void vecAdd(float[] a, float[] b, float c[], int n) {
		
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

