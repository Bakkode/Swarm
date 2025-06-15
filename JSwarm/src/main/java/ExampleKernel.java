import java.util.concurrent.CyclicBarrier;

import io.github.seal139.jSwarm.datatype.FloatVector;
import io.github.seal139.jSwarm.runtime.Program;

public final class ExampleKernel extends Program {

    public ExampleKernel(CyclicBarrier barrier, int globalX, int localX, int totalX, int currentGlobalX, int currentLocalX, int currentX, int globalY,
            int localY, int totalY, int currentGlobalY, int currentLocalY, int currentY, int globalZ, int localZ, int totalZ, int currentGlobalZ,
            int currentLocalZ, int currentZ) {
        super(barrier, globalX, localX, totalX, currentGlobalX, currentLocalX, currentX, globalY, localY, totalY, currentGlobalY, currentLocalY,
                currentY, globalZ, localZ, totalZ, currentGlobalZ, currentLocalZ, currentZ);
    }

    public void vecAdd(FloatVector a, FloatVector b, FloatVector c, FloatVector d, float e) {
        int idx = this.currentRangeX;
        int idy = this.currentRangeY;

        int scIndex = (this.currentRangeX * 6) + idy;

        float t = get(a, idx) * get(b, idy);

        set(c, scIndex, t);

        synchronize();

        float summ = 0;

        int max = this.totalRangeX * this.totalRangeY;
        for (long i = 0; i < max; i++) {
            summ += get(c, i);
        }

        set(d, scIndex, e * summ);
    }
}
