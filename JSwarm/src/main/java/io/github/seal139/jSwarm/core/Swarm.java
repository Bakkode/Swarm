package io.github.seal139.jSwarm.core;

import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.Set;

import io.github.seal139.jSwarm.backend.cuda.Cuda;
import io.github.seal139.jSwarm.backend.hipAmd.HipAmd;
import io.github.seal139.jSwarm.backend.hipNv.HipNv;
import io.github.seal139.jSwarm.backend.jvm.Jvm;
import io.github.seal139.jSwarm.backend.openCl.OpenCL;

public final class Swarm {
	
	private static final Set<Class<? extends Platform>> driver = new HashSet<>();
	private static final boolean debug;
	static {
		{
			driver.add(Jvm.class);
			driver.add(Cuda.class);
			driver.add(HipAmd.class);
			driver.add(HipNv.class);
			driver.add(OpenCL.class);
		}
		
		{
			String[] arguments = ManagementFactory.getRuntimeMXBean().getInputArguments().toArray(new String[0]);
			boolean d = false;
			for (String arg : arguments) {
			    if (d = arg.contains("jdwp") || arg.contains("-Xdebug")) {
			    	break;
			    }
			}

			debug = d;
		}
	}
	
	public static boolean isDebugMode() {
		return debug;
	}

	public static void init() {
		
	}
	
	public static void register(Class<? extends Platform> driver) {
		
	}
	
	public static String[] enumeratePlatformName() {
		
	}
	
	public static Executor[] getDevices() {
		
	}
	
	public static Executor[] getDevices(String platformName) {
		
	}
	
	public static Executor getDevice() {
		
	}
	
	public static Executor getDevice(String platformName) {
		
	}
}
