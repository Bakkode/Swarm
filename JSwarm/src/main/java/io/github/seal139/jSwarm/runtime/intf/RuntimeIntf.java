package io.github.seal139.jSwarm.runtime.intf;

import io.github.seal139.jSwarm.hardware.HardwareInfo;
import io.github.seal139.jSwarm.runtime.Kernel;

public interface RuntimeIntf {
	
	void setDevice(HardwareInfo hardware);
	
	Kernel getKernel(String name);
	
	
}
