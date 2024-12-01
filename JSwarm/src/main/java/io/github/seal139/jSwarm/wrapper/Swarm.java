package io.github.seal139.jSwarm.wrapper;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.seal139.jSwarm.backend.Cuda;
import io.github.seal139.jSwarm.backend.HipAmd;
import io.github.seal139.jSwarm.backend.HipNv;
import io.github.seal139.jSwarm.backend.Jvm;
import io.github.seal139.jSwarm.backend.OpenCL;
import io.github.seal139.jSwarm.core.BackendIntf;
import io.github.seal139.jSwarm.core.Device;
import io.github.seal139.jSwarm.core.Kernel;
import io.github.seal139.jSwarm.core.Module;
import io.github.seal139.jSwarm.runtime.Program;

public class Swarm {
	
	private static final boolean debug;
	public static final boolean isDebug() {
		return debug;
	}
	
	//============
	
	private static boolean profiling = false;
	public static final void setProfiling(boolean p) {
		profiling = p;
	}
	
	public static final boolean isProfiling() {
		return profiling;
	}
	
	//============
	
	private static final Map<BackendIntf, Integer> backend = new HashMap<>();
	
	static {
		{
			String[] arguments = ManagementFactory.getRuntimeMXBean().getInputArguments().toArray(new String[0]);
			boolean d = false;
			for (String arg : arguments) {
			    if (arg.contains("jdwp") || arg.contains("-Xdebug")) {
			    	d = true;
			    	break;
			    }
			}

			debug = d;
		}
		
		
		//backend.put(Jvm.getInstance(), 5);	   // In case for debugging or when no GPGPU API available
		//backend.put(OpenCL.getInstance(), 5);  // OpenCL for wider hardware support
		//backend.put(HipNv.getInstance(), 10);  // HIP for Nvidia
		//backend.put(HipAmd.getInstance(), 11); // HIP for AMD
		//backend.put(Cuda.getInstance(), 11);   // Nvidia with cuda is more prefered than HIP for Nvidia
	}
	
	public static void addBackend(BackendIntf b, int priority) {
		Swarm.backend.put(b, priority);
	}
	
	private Swarm() {
		
	}
	
	public Module loadProgram(Device Device, List<Class<? extends Program>> programs) {
		return null;
	}
	
	public Module loadProgram(Device device, Class<? extends Program> ... programs) {
		return null;	
	}
	
	public Kernel getKernel(Module module, String kernelName) {
		return null;
	}
	
}
