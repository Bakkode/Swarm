import io.github.seal139.jSwarm.core.Program;

public final class ExampleKernel extends Program {

    public void vecAdd(float[] a, float[] b, float c[], int n) {

        int id = 1; // getGlobalId(0);

        if (id < n) {
            c[id] = a[id] + b[id];
        }
    }

    @SharedMemory(name = "tmp", type = DataType.FLOAT)
    protected void vecSub(@Global float[] a, //
                          @Global float[] b, //
                          @Global float c[], //
                          int n) {

        int id = getGlobalId();

        if (id < n) {
            c[id] = a[id] - b[id];
        }
    }
}
