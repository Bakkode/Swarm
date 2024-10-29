import io.github.seal139.jSwarm.CudaKernel;
import io.github.seal139.jSwarm.Fp32;
import io.github.seal139.jSwarm.Int32;

public class Test {
	
	
	
	
	
	public static final class SampleKernel extends CudaKernel {
	
		public void quadraticFormula(Int32 a, Int32 b, Int32 c) {
			Fp32 b2  = Fp32.ofValue(pow(b, 2));
			Fp32 ac4 = Fp32.ofValue(mul(-4, mul(a, c)));
			
			Fp32 b2m4acRoot = Fp32.ofValue(pow(sub(b2, ac4), 0.5));
			
			Fp32 a2 = Fp32.ofValue(mul(2, a));
			
			Fp32 mb = Fp32.ofValue(mul(-1, b));
			
			Fp32 r1 = (Fp32) add(mb, b2m4acRoot);
			Fp32 r2 = (Fp32) sub(mb, b2m4acRoot);
			
			Fp32 rr1 = Fp32.ofValue(div(r1, a2));
			Fp32 rr2 = Fp32.ofValue(div(r2, a2));
			
		}
	}
}
