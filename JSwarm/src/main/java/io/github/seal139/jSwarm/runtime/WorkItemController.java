package io.github.seal139.jSwarm.runtime;

import java.lang.invoke.MethodHandle;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

public class WorkItemController implements Runnable {

    public WorkItemController(CountDownLatch latch, Supplier<Program> obj, MethodHandle method, Object[] param, NdRange range, int start, int end) {
        this.latch = latch;
        this.ctor  = obj;

        this.method = method;

        this.param = param;
        this.range = range;

        this.start = start;
        this.stop  = end;
    }

    private final NdRange           range;
    private final Supplier<Program> ctor;
    private final MethodHandle      method;

    private final Object[]       param;
    private final CountDownLatch latch;

    private final int start;
    private final int stop;

    @Override
    public void run() {
        try {
            // Cache variable to method for faster access speed
            final Program cacheObj = this.ctor.get();

            final MethodHandle cacheMethod = this.method.bindTo(cacheObj);

            final Object[] cacheParam = this.param;
            final NdRange  cacheRange = this.range;

            final int cacheStart = this.start;
            final int cacheStop  = this.stop;

            final int lx = cacheRange.getXLocal();
            final int ly = cacheRange.getYLocal();
            final int lz = cacheRange.getZLocal();

            final int gx = cacheRange.getXGlobal();
            final int gy = cacheRange.getYGlobal();
            final int gz = cacheRange.getZGlobal();

            cacheObj.setNdRange(//
                    lx, gx, cacheRange.getTotalX(), //
                    ly, gy, cacheRange.getTotalY(), //
                    lz, gz, cacheRange.getTotalZ() //
            );

            int itm;

//            ExampleKernel ek = (ExampleKernel) cacheObj;
//            FloatVector   i1 = (FloatVector) cacheParam[0];
//            FloatVector   i2 = (FloatVector) cacheParam[1];
//            FloatVector   o  = (FloatVector) cacheParam[2];

            for (int linearId = cacheStart; linearId < cacheStop; linearId++) {
                // Convert 1D index back to 3D (Z, Y, X)
                itm = linearId / lx;

                cacheObj.setLocalRange((linearId % lx), (itm % ly), (itm / ly));

                for (int x = 0, xx = 0; x < gx; xx += lx) {
                    cacheObj.setCurrentGlobalRangeX(x++, xx);

                    for (int y = 0, yy = 0; y < gy; yy += ly) {
                        cacheObj.setCurrentGlobalRangeY(y++, yy);

                        for (int z = 0, zz = 0; z < gz; zz += lz) {
                            cacheObj.setCurrentGlobalRangeZ(z++, zz);

//                           ek.matrixMultiplication(i1, i2, o);
                            cacheMethod.invoke(cacheParam);
                        }
                    }
                }
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
        finally {
            this.latch.countDown();
        }

    }
}
