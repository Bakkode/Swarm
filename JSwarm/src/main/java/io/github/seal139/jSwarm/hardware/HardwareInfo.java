package io.github.seal139.jSwarm.hardware;


public class HardwareInfo {
	
	private final HardwareType type;
	private final PlatformType platform;
	private final String platformVersion;
	
	private final String deviceName;
	private final int computeUnit;
	private final int globalMemSize;
	private final int localMemSize;
	
	private final int[] maxDimension;
	private final int depth;
	
	private final int flops;
	
	public HardwareInfo(
			int type,
			int platform,
			String version,
			
			String name,
			int cu,
			int globMem,
			int locMem,
			
			int dimX,
			int dimY,
			int dimZ,
			int dimD,
			
			int flops			
	) {
		this.type            = HardwareType.values()[type];
		this.platform        = PlatformType.values()[platform];
		this.platformVersion = version;
		
		this.deviceName    = name;
		this.computeUnit   = cu;
		this.globalMemSize = globMem;
		this.localMemSize  = locMem;
		
		this.maxDimension = new int[] {dimX, dimY, dimZ};
		this.depth        = dimD;
		
		this.flops = flops;
	}

	public HardwareType getType() {
		return type;
	}

	public PlatformType getPlatform() {
		return platform;
	}

	public String getPlatformVersion() {
		return platformVersion;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public int getComputeUnit() {
		return computeUnit;
	}

	public int getGlobalMemSize() {
		return globalMemSize;
	}

	public int getLocalMemSize() {
		return localMemSize;
	}

	public int[] getMaxDimension() {
		return maxDimension;
	}

	public int getDepth() {
		return depth;
	}

	public int getFlops() {
		return flops;
	}
	
}
