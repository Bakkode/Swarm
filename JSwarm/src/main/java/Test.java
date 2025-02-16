import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import io.github.seal139.jSwarm.backend.cuda.Cuda;
import io.github.seal139.jSwarm.core.Context;
import io.github.seal139.jSwarm.core.Executor;
import io.github.seal139.jSwarm.core.Kernel;
import io.github.seal139.jSwarm.core.Module;
import io.github.seal139.jSwarm.core.NativeException;
import io.github.seal139.jSwarm.core.NdRange;
import io.github.seal139.jSwarm.core.SwarmException;
import io.github.seal139.jSwarm.core.SyncDirection;
import io.github.seal139.jSwarm.datatype.FloatArray;
import io.github.seal139.jSwarm.datatype.Vector;
import io.github.seal139.jSwarm.misc.Decompiler;

public class Test {
    @SuppressWarnings("unchecked")
    public static void cudaTest() throws Error, SwarmException, Exception {
        Cuda     cuda   = Cuda.getInstance();
        Executor device = cuda.getDevices()[0];

        try (//
                Context ctx = device.getDefaultContext(); //
                Module module = ctx.loadProgram("""
                        extern "C" __global__ void vecAdd(const float* a, const float* b, float* c, const float* n) {
                            int idx = blockIdx.x * blockDim.x + threadIdx.x;
                            if (idx < (int)*n) {
                                c[idx] = a[idx] + b[idx];
                            }
                        }
                        """); //
                //
                Vector<Float> i1 = new FloatArray(5, true); //
                Vector<Float> i2 = new FloatArray(5, true); //
                Vector<Float> o1 = new FloatArray(5, true); //
                Vector<Float> n = new FloatArray(1, true);) {

            for (long i = 0; i < 5; i++) {
                i1.set(i, (1 + i) * 1.0f);
                i2.set(i, (6 + i) * 1.0f);
            }
            n.set(0L, 5f);

            ctx.hook(i1);
            ctx.hook(i2);
            ctx.hook(o1);
            ctx.hook(n);

            ctx.sync(SyncDirection.TO_DEVICE, i1, i2, n);
            ctx.waitOperation();

            Kernel addKernel = module.getKernel("vecAdd");
            ctx.launch(addKernel, NdRange.OneDimensional(5), i1, i2, o1, n);
            ctx.waitOperation();

            ctx.sync(SyncDirection.TO_HOST, o1);
            ctx.waitOperation();

            o1.forEach(v -> System.out.println(v));
            System.out.println("End");
        }
    }

    public static void boo() {

        String classPath = "/" + Test.Expl.class.getName().replace('.', '/') + ".class";

        try {
            // Run javap command to disassemble bytecode of the class
            ProcessBuilder pb = new ProcessBuilder("javap", "-c", Test.Expl.class.getName());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Read and print the output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            // Wait for the command to finish
            process.waitFor();
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void testVectorSync() throws Exception {
        List<Float> av = List.of(20.2f, 14.25f, 6.179f);

        List<Float> ff = new ArrayList<>(100_000_000);
        try (Vector<Float> fv = new FloatArray(100_000_000, false)) {
            for (int i = 0; i < 100_000_000; i++) {
                ff.add(1.5f * i);
                fv.add(1.5f * i);
            }

            fv.set(40, 128.82f);
            ff.set(40, 128.82f);

            int i = 0;
            for (Float f : ff) {
                if (!fv.get(i).equals(f)) {
                    System.out.println("nah ngaco: " + i);
                    System.out.println(f);
                    System.out.println(fv.get(i));
                }
                i += 1;
            }

            System.out.println("Integrity test of " + i + " (" + fv.size() + ") data ok");

            ff.clear();
            fv.clear();

            for (int j = 0; j < 32_000_000; j++) {
                ff.add(1.5f * j);
                fv.add(1.5f * j);
            }

            List<Float> f1 = ff.subList(30_000_000, 32_000_000);
            List<Float> f2 = fv.subList(30_000_000, 32_000_000L);

            for (int ii = 1_999_990; ii < 2_000_000; ii++) {
                f1.set(ii, 1.5f * ii);
                f2.set(ii, 1.5f * ii);
            }

            ff.remove(31_999_999);
            fv.remove(31_999_999L);

            ff.addAll(av);
            fv.addAll(av);

            int j = 0;
            for (Float f : ff) {
                if (!fv.get(j).equals(f)) {
                    System.out.println("nah ngaco");
                }
                j += 1;
            }

            System.out.println("Integrity test of " + j + " (" + fv.size() + ") data ok");
        }
    }

    private static void testPerformanceComparison() throws NativeException, Exception {

        int         max      = 100_000;
        List<Float> javaList = new ArrayList<>();

        float ff = 1.5f;

        long ctr = System.nanoTime();
        for (int i = 0; i < max; i++) {
            javaList.add(ff);
        }
        ctr = System.nanoTime() - ctr;
        System.out.println("-Java list insertion took " + (ctr / 1000000.0) + "ms");

        Float rf = 0f;
        ctr = System.nanoTime();
        for (Float f : javaList) {
            rf = f;
        }
        ctr  = System.nanoTime() - ctr;
        rf  += 1;
        System.out.println("-Java list iteration took " + (ctr / 1000000.0) + "ms");

        try (FloatArray fv = new FloatArray(8192, true);) {
            ctr = System.nanoTime();
            for (int i = 0; i < max; i++) {
                fv.add(ff);
            }

            ctr = System.nanoTime() - ctr;
            System.out.println("-Native list insertion took " + (ctr / 1000000.0) + "ms");

            float nf = 0f;
            ctr = System.nanoTime();
            for (Float f : fv) {
                nf = f;
            }
            ctr  = System.nanoTime() - ctr;
            nf  += 1;
            System.out.println("-Native list iteration took " + (ctr / 1000000.0) + "ms");
        }

    }

    private static void benchFloatArray() throws Exception {
        int dir = 1073741824;
        for (long j = dir; j >= 2; j /= 2) {
            long jj = j == 0 ? 1 : j;

            float ff  = 1.5f;
            long  max = jj;  // * 1_000_000;

            for (int k = 0; k < 1; k++) {

                // 8 * 8192, 8192, 4
                try (FloatArray fv = new FloatArray(dir);) { // buffer * 2

                    long ctr = System.nanoTime();
                    for (int i = 0; i < max; i++) {
                        fv.add(ff);
                    }

                    ctr = System.nanoTime() - ctr;

                    System.out.println("-Insertion of " + fv.size() + " data: " + (ctr /* / 1000000.0 */) + "ns");

                    Float f;
                    int   c = 0;
                    ctr = System.nanoTime();
                    for (Float fl : fv) {
                        c += 1;
                        f  = fl;
                    }
                    ctr = System.nanoTime() - ctr;

                    System.out.println("-iteration of " + c + " data: " + (ctr) + "ns");
                }
            }
        }
    }

    private static void benchFloatVector(boolean dynamic) throws Exception {
        // 262136, 16

        // 131068, 64: 15-21ms
        // I think this is the most optimal. More node with Smaller buffer for insertion
        // 32 buckets cause collision

        // 8192, 16: 16-21ms // 32kb L1 caching
        // Based on benchmark, this is the optimal. i think this due to L1 caching
        // multiply by 2 for every 9,000,000

        int dir = 1073741824;
        for (long j = dir; j >= 2; j /= 2) {
            long jj = j == 0 ? 1 : j;

            float ff  = 1.5f;
            long  max = jj;  // * 1_000_000;

            for (int k = 0; k < 10; k++) {

                // 8 * 8192, 8192, 4
                try (Vector<Float> fv = new FloatArray(j, true);) { // buffer * 2

                    long ctr = System.nanoTime();
                    ctr = System.nanoTime();
                    for (int i = 0; i < j; i++) {
                        fv.set(i, 10f);
                    }
                    ctr = System.nanoTime() - ctr;
                    System.out.println("- Rand  Write of " + j + " data: " + (ctr) + "ns");

                    ctr = System.nanoTime();
                    for (int i = 0; i < max; i++) {
                        fv.add(ff);
                    }
                    ctr = System.nanoTime() - ctr;
                    System.out.println("- Seq  Insert of " + fv.size() + " data: " + (ctr /* / 1000000.0 */) + "ns");

                    int c = 0;
                    ctr = System.nanoTime();
                    for (Float fl : fv) {
                        c += 1;
                    }
                    ctr = System.nanoTime() - ctr;
                    System.out.println("- Seq Iterate of " + c + " data: " + (ctr) + "ns");

                    ctr = System.nanoTime();
                    for (int i = 0; i < j; i++) {
                        fv.get(i);
                    }
                    ctr = System.nanoTime() - ctr;
                    System.out.println("-Rand iterate of " + c + " data: " + (ctr) + "ns");
                }
            }
        }

    }

    public static void main(String... strings) throws Exception {
        cudaTest();

        int[] a = {
                1, 2, 3, 4, 5 };

        int[] b = a;

        b[1] = 20;

        long l = 7_768_998_213L;
        System.out.println((int) l);

        testVectorSync();
//        testPerformanceComparison();

//        benchFloatVector(true);
//        System.out.println("====");
        benchFloatVector(true);

        // benchFloatArray();
        // writeFloatVector();

//        Test t = new Test();
//
//        t.decompile();
    }

    public void decompile() throws Exception {
        String classPath = "/" + ExampleKernel.class.getName().replace('.', '/') + ".class";

        Decompiler d = Decompiler.getDefault();

        String s1 = d.process(List.of(ExampleKernel.class), this::processCuda);

        System.out.println();

        System.out.println(s1);
        System.out.println("\n\n\n============\n\n\n");

        try (InputStream input = Test.class.getResourceAsStream(classPath)) {
            if (input != null) {
                System.out.println("Bytecode for " + ":");
                byte[] buffer = input.readAllBytes();

                // Print bytecode in hexadecimal format
                for (byte b : buffer) {
                    System.out.printf("%02x ", b);
                }
                System.out.println();
            }
            else {
                System.out.println("Class not found in the JAR.");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String processCuda(String s) {
        return s;
    }

    public static final class Expl {

        public static void doAnything(int[] ret, int[] va, int[] vb, int[] vc) {
            ret[0] = (va[0] * vb[0]) + vc[0];
        }
    }
}
