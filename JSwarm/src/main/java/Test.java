import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import io.github.seal139.jSwarm.backend.Context;
import io.github.seal139.jSwarm.backend.Executor;
import io.github.seal139.jSwarm.backend.Kernel;
import io.github.seal139.jSwarm.backend.Module;
import io.github.seal139.jSwarm.backend.Platform;
import io.github.seal139.jSwarm.backend.cuda.Cuda;
import io.github.seal139.jSwarm.backend.jvm.Jvm;
import io.github.seal139.jSwarm.backend.ocl.Ocl;
import io.github.seal139.jSwarm.datatype.FloatVector;
import io.github.seal139.jSwarm.datatype.Vector;
import io.github.seal139.jSwarm.runtime.NdRange;
import io.github.seal139.jSwarm.runtime.SyncDirection;

public class Test {

    private static void hardwareEnumerator(Platform platform) {
        System.out.println("----------" + platform.getName() + " - " + platform.getFullName() + " v" + platform.getVersion() + "--------- \n");
        for (Executor dev : platform.getDevices()) {

            System.out.println(dev.getUuid() + ": " + dev.getName());
            System.out.println(dev.getFlops() + " GFLOPS");
            System.out.println("Compute Unit: " + String.valueOf(dev.getComputeUnit()));
            System.out.println("Total Memory: " + String.valueOf(dev.getTotalMemory() / 1049000000) + "Gb");
            System.out.println("NDRange: " + dev.getMaxNDRange());
            System.out.println("Max Global NDRange [" //
                    + String.valueOf(dev.getMaxGlobalSize()[0]) + ", " //
                    + String.valueOf(dev.getMaxGlobalSize()[1]) + ", " //
                    + String.valueOf(dev.getMaxGlobalSize()[2]) + "]");

            System.out.println("Max Local NDRange [" //
                    + String.valueOf(dev.getMaxLocalSize()[0]) + ", " //
                    + String.valueOf(dev.getMaxLocalSize()[1]) + ", " //
                    + String.valueOf(dev.getMaxLocalSize()[2]) + "]");

            System.out.println("Max Local Thread: " + String.valueOf(dev.getMaxLocalThread()));

            System.out.println("");
        }

        System.out.println("");
    }

    private static void runKernel(Platform platform) {
        Executor device = platform.getDevices()[0];

        try {
            Context ctx = device.getDefaultContext();
            ctx.activate();
            Module module = ctx.loadProgram(ExampleKernel.class); //

            //
            Vector<Float> i1 = new FloatVector(6, true); //
            Vector<Float> i2 = new FloatVector(6, true); //

            Vector<Float> o1 = new FloatVector(36, true); //
            Vector<Float> o2 = new FloatVector(36, true); //

            for (long i = 0; i < 6; i++) {
                i1.set(i, (1 + i) * 1.0f);
                i2.set(i, (6 + i) * 1.0f);
            }

            ctx.hook(i1);
            ctx.hook(i2);
            ctx.hook(o1);
            ctx.hook(o2);

            //

            ctx.sync(SyncDirection.TO_DEVICE, i1, i2);
            ctx.waitOperation();

            Kernel addKernel = module.getKernel("vecAdd");
            ctx.launch(addKernel, NdRange.TwoDimensional(2, 2, 3, 3), i1, i2, o1, o2, 1.5f);
            ctx.waitOperation();

            ctx.sync(SyncDirection.TO_HOST, o1, o2);
            ctx.waitOperation();

            ctx.unhook(i1);
            ctx.unhook(i2);
            ctx.unhook(o1);
            ctx.unhook(o2);

            o1.forEach(v -> System.out.println(v));

            i1.close();
            i2.close();
            o1.close();
            o2.close();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testVectorSync() throws Exception {
        List<Float> av = List.of(20.2f, 14.25f, 6.179f);

        List<Float> ff = new ArrayList<>(100_000_000);
        try (Vector<Float> fv = new FloatVector(100_000_000, false)) {
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

    private static void testPerformanceComparison() throws Exception {

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

        try (FloatVector fv = new FloatVector(max, true);) {
            ctr = System.nanoTime();
            for (int i = 0; i < max; i++) {
                fv.set(i, ff);
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
                try (FloatVector fv = new FloatVector(dir);) { // buffer * 2

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

        int dir = 1073741824;
        for (long j = dir; j >= 2; j /= 2) {
            long jj = j == 0 ? 1 : j;

            float ff  = 1.5f;
            long  max = jj;  // * 1_000_000;

            for (int k = 0; k < 10; k++) {

                // 8 * 8192, 8192, 4
                try (Vector<Float> fv = new FloatVector(j, true);) { // buffer * 2

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
        hardwareEnumerator(Cuda.getInstance());
        hardwareEnumerator(Ocl.getInstance());
        hardwareEnumerator(Jvm.getInstance());

        runKernel(Jvm.getInstance());
        System.out.println("---");
        runKernel(Cuda.getInstance());
        System.out.println("---");
        runKernel(Ocl.getInstance());
        System.out.println("---");

        testPerformanceComparison();

        System.out.println("===");

        int count = 20;

        final CyclicBarrier cb  = new CyclicBarrier(count);
        ForkJoinPool        fjp = new ForkJoinPool(20);

        int value[] = {
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

        int a[] = {
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };

        int b[] = {
                2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 5, 5, 5, 5, 5, 7, 7, 7, 7, 7 };

        for (int i = 0; i < count; i++) {
            final int c = i;

            fjp.submit(() -> {

                ExampleKernel x = new ExampleKernel(cb, count, count, count, count, count, count, count, count, count, count, count, count, count,
                        count, count, count, count, count);

                try {
                    // x.execute(c, value, a, b);
                }
                catch (Exception e) {
                    // TODO: handle exception
                }
            });
        }

        fjp.awaitQuiescence(30, TimeUnit.SECONDS);

//        cudaTest();
//
//        int[] a = {
//                1, 2, 3, 4, 5 };
//
//        int[] b = a;
//
//        b[1] = 20;
//
//        long l = 7_768_998_213L;
//        System.out.println((int) l);
//
//        testVectorSync();
////        testPerformanceComparison();
//
////        benchFloatVector(true);
////        System.out.println("====");
//        benchFloatVector(true);

        // benchFloatArray();
        // writeFloatVector();
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
