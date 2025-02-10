package io.github.seal139.jSwarm.core;

public class NdRange {

    private final int x, y, z;
    private final int lx, ly, lz;

    private NdRange(int x, int lx, int y, int ly, int z, int lz) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.lx = lx;
        this.ly = ly;
        this.lz = lz;
    }

    public static NdRange OneDimensional(int maxGlobalRange) {
        return new NdRange(maxGlobalRange, 1, 1, 1, 1, 1);
    }

    public static NdRange TwoDimensional(int maxGlobalRangeX, int maxGlobalRangeY) {

        return new NdRange(maxGlobalRangeX, 1, maxGlobalRangeY, 1, 1, 1);
    }

    public static NdRange ThreeDimensional(int maxGlobalRangeX, int maxGlobalRangeY, int maxGlobalRangeZ) {
        return new NdRange(maxGlobalRangeX, 1, maxGlobalRangeY, 1, maxGlobalRangeZ, 1);
    }

    public static NdRange OneDimensional(int maxGlobalRange, int maxLocalRange) {
        return new NdRange(maxGlobalRange, maxLocalRange, 1, 1, 1, 1);
    }

    public static NdRange TwoDimensional(int maxGlobalRangeX, int maxGlobalRangeY, //
                                         int maxLocalRangeX, int maxLocalRangeY) {

        return new NdRange(maxGlobalRangeX, maxLocalRangeX, maxGlobalRangeY, maxLocalRangeY, 1, 1);
    }

    public static NdRange ThreeDimensional(int maxGlobalRangeX, int maxGlobalRangeY, int maxGlobalRangeZ, //
                                           int maxLocalRangeX, int maxLocalRangeY, int maxLocalRangeZ) {

        return new NdRange(maxGlobalRangeX, maxLocalRangeX, maxGlobalRangeY, maxLocalRangeY, maxGlobalRangeZ, maxLocalRangeZ);
    }

    public int getXGlobal() { return this.x; }

    public int getYGlobal() { return this.y; }

    public int getZGlobal() { return this.z; }

    public int getXlocal() { return this.lx; }

    public int getYLocal() { return this.ly; }

    public int getZLocal() { return this.lz; }
}
