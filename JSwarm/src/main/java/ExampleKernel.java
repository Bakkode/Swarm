import java.util.concurrent.CyclicBarrier;

import io.github.seal139.jSwarm.core.Program;
import io.github.seal139.jSwarm.datatype.FloatVector;
import io.github.seal139.jSwarm.datatype.IntVector;

public final class ExampleKernel extends Program {

    public ExampleKernel(CyclicBarrier barrier, int globalX, int localX, int totalX, int currentGlobalX, int currentLocalX, int currentX, int globalY,
            int localY, int totalY, int currentGlobalY, int currentLocalY, int currentY, int globalZ, int localZ, int totalZ, int currentGlobalZ,
            int currentLocalZ, int currentZ) {
        super(barrier, globalX, localX, totalX, currentGlobalX, currentLocalX, currentX, globalY, localY, totalY, currentGlobalY, currentLocalY,
                currentY, globalZ, localZ, totalZ, currentGlobalZ, currentLocalZ, currentZ);
    }

    public void vecAdd(FloatVector a, FloatVector b, FloatVector c, IntVector n, float d) {
        int idx = this.currentRangeX;
        if (idx < get(n, 0)) {
            set(c, idx, (d + get(a, idx) + get(b, idx)));
        }
    }
}
