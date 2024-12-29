package io.github.seal139.jSwarm.hardware;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import io.github.seal139.jSwarm.misc.Common;

public final class Hardware {
	public static native HardwareInfo[] enumerateCudaDevice();
	public static native HardwareInfo[] enumerateRocmDevice();
	public static native HardwareInfo[] enumerateOpClDevice();
	
	
	private static boolean clEnabled;
	private static boolean cdEnabled;
	private static boolean roEnabled;
	
	// Load the C++ shared library
    static {
    	try {
            System.load("D:/swarmsimd.dll");
            clEnabled = false;
            cdEnabled = true;
            roEnabled = false;
            
    	}catch (Throwable e) {
    		e.printStackTrace();
    		
    		clEnabled = false;
    		cdEnabled = false;
    		roEnabled = false;
    	}
    }
    
    private static Hardware instance = new Hardware();
    
    public static Hardware getInstance() {
    	return instance;
    }
    
    private final List<HardwareInfo> hws = new ArrayList<>();
    
    private Hardware() {
    	// Nvidia CUDA
    	if(cdEnabled) {
    		HardwareInfo[] hws = enumerateCudaDevice();
//    		for(HardwareInfo hw : hws) {
//    			this.hws.add(hw);	
//    		}
    	}
    	
    	// AMD HIP - ROCm
    	if(roEnabled) {
    		HardwareInfo[] hws = enumerateRocmDevice();
    		for(HardwareInfo hw : hws) {
    			this.hws.add(hw);	
    		}
    	}
    	
    	// OpenCL
    	if(clEnabled) {
    		HardwareInfo[] hws = enumerateOpClDevice();
    		for(HardwareInfo hw : hws) {
    			this.hws.add(hw);	
    		}
    	}
    }
    
    public HardwareInfo[] getHardwareList() {
    	// To make sure internal hardware list is not modified
    	final int size = this.hws.size();
    	HardwareInfo[] hw = new HardwareInfo[size];
    	
    	for(int i = 0; i < size; i++) {
    		hw[i] = this.hws.get(i);
    	}
    	
    	return hw;
    }
    
    
    public static void main(String ...strings ) throws Exception {
    	Hardware hw = getInstance();
    	
    	HardwareInfo[] hws = hw.getHardwareList();

    	
    	ForkJoinPool pool  = new ForkJoinPool(1);
    	
    	for(int i = 0; i < 20; i++) {
    		Common.queue(() ->{
        		System.out.println("Called by: " +Thread.currentThread().getId()); 
        		
        		pool.execute(() -> {
        			System.out.println("executed by: " +Thread.currentThread().getId()); 
        		});
        		return null;
        	});
    	}
    	
    	
    	Common.await("", 1);
    

    	System.out.println("mbo");
    	
    }
    
    
}
