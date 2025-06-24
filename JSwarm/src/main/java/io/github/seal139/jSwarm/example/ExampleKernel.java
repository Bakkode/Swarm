package io.github.seal139.jSwarm.example;

import io.github.seal139.jSwarm.datatype.FloatVector;
import io.github.seal139.jSwarm.runtime.Program;

public final class ExampleKernel extends Program {

    private int getScalarIndex(int x, int y) {
        return((x * this.localRangeX()) + y);
    }

    public void matrixMultiplication(FloatVector matrix1, FloatVector matrix2, FloatVector result) {
        int y = this.currentLocalRangeY();
        int x = this.currentLocalRangeX();

        float temp = 0.0f;
        for (int i = 0; i < this.localRangeX(); i++) {
            float xi = get(matrix1, getScalarIndex(i, y));
            float yi = get(matrix2, getScalarIndex(x, i));

            // temp += (xi * yi);
            temp = fmafp32(xi, yi, temp);
        }

        set(result, getScalarIndex(x, y), temp);
    }
}
