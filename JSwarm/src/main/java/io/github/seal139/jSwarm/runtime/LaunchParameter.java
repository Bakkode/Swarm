package io.github.seal139.jSwarm.runtime;

public final class LaunchParameter {
	private final int x;
	private final int y;
	private final int z;
	
	private final int d;
	
	public LaunchParameter(int x, int y, int z, int depth) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.d = depth;
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
}
