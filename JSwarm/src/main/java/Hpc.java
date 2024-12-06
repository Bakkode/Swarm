public class Hpc {	

	private static void printDevice(HardwareInfo hi) {
		System.out.println("Type\t\t\t: "     + hi.getType().name());
		System.out.println("Platform\t\t: "   + hi.getPlatform().name());
		System.out.println("Version\t\t\t: "  + hi.getPlatformVersion());
		
		System.out.println("Device Name\t\t: "    + hi.getDeviceName());
		System.out.println("Computation Unit\t: " + hi.getComputeUnit());
		System.out.println("Heap Size\t\t: "      + hi.getGlobalMemSize());
		System.out.println("Stack Size\t\t: "     + hi.getLocalMemSize());
		
		System.out.println("Max Dimension\t\t: [" + hi.getMaxDimension()[0] + ", " + hi.getMaxDimension()[1] + ", " + hi.getMaxDimension()[2] + "]");
		System.out.println("Depth\t\t\t: "    + hi.getDepth());
		System.out.println("Processing Power\t: "    + hi.getFlops());
	}
	
	public static void main(String[] args) {
		
		Hardware hw = Hardware.getInstance();
		
		HardwareInfo[] tmp = hw.getHardwareList();
		
		for(HardwareInfo hi : tmp) {
			printDevice(hi);
			
			System.out.println("\n\n");
		}
   }
}
