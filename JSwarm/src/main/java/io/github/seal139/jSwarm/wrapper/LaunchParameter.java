package io.github.seal139.jSwarm.wrapper;

import java.util.ArrayList;
import java.util.List;

import io.github.seal139.jSwarm.runtime.datatype.Vector;

public final class LaunchParameter {
	private final int x;
	private final int y;
	private final int z;
	
	private final int d;
	
	private final List<Vector<?>> arguments = new ArrayList<>();
	
	public LaunchParameter(int x, int y, int z, int depth) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.d = depth;
	}

	public void addArguments(Vector<?> arg) {
		arguments.add(arg);
	}
	
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	public int getDepth() {
		return d;
	}
	
	public Vector<?>[] getArguments() {
		Vector<?>[] ret = new Vector<?>[arguments.size()];
		for(int i = 0; i < arguments.size(); i++) {
			ret[i] = arguments.get(i);
		}
		
		return ret;
	}
	
	
}
