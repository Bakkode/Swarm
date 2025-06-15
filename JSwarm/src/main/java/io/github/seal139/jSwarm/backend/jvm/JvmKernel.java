package io.github.seal139.jSwarm.backend.jvm;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import io.github.seal139.jSwarm.backend.BackendException;
import io.github.seal139.jSwarm.backend.Kernel;
import io.github.seal139.jSwarm.runtime.NdRange;
import io.github.seal139.jSwarm.runtime.Program;

public class JvmKernel implements Kernel {

    private static final ForkJoinPool exec = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

    private final Method         method;
    private final Constructor<?> constr;

    JvmKernel(Constructor<?> constr, Method method) {
        this.method = method;
        this.constr = constr;
    }

    void run(NdRange range, Number... param) throws BackendException {

        // Max indexer
        final int maxX = range.getXGlobal() * range.getXLocal();
        final int maxY = range.getYGlobal() * range.getYLocal();
        final int maxZ = range.getZGlobal() * range.getZLocal();

        final CyclicBarrier barrier = new CyclicBarrier(maxX * maxY * maxZ);

        // Current indexer
        for (int currGlobX = 0, currX = 0; currGlobX < range.getXGlobal(); currGlobX++) {
            for (int currloclX = 0; currloclX < range.getXLocal(); currloclX++, currX++) {

                for (int currGlobY = 0, currY = 0; currGlobY < range.getYGlobal(); currGlobY++) {
                    for (int currloclY = 0; currloclY < range.getYLocal(); currloclY++, currY++) {

                        for (int currGlobZ = 0, currZ = 0; currGlobZ < range.getZGlobal(); currGlobZ++) {
                            for (int currloclZ = 0; currloclZ < range.getZLocal(); currloclZ++, currZ++) {
                                _run(barrier, //
                                        range.getXGlobal(), range.getXLocal(), maxX, currGlobX, currloclX, currX, //
                                        range.getYGlobal(), range.getYLocal(), maxY, currGlobY, currloclY, currY, //
                                        range.getZGlobal(), range.getZLocal(), maxZ, currGlobZ, currloclZ, currZ, //
                                        param);

                            }
                        }
                    }

                }
            }
        }
    }

    private void _run(CyclicBarrier barrier, //
                      int globalX, int localX, int totalX, int currentGlobalX, int currentLocalX, int currentX, //
                      int globalY, int localY, int totalY, int currentGlobalY, int currentLocalY, int currentY, //
                      int globalZ, int localZ, int totalZ, int currentGlobalZ, int currentLocalZ, int currentZ, //
                      Number... param) {

        exec.execute(() -> {
            Program obj;
            try {
                obj = (Program) this.constr.newInstance(barrier, //
                        globalX, localX, totalX, currentGlobalX, currentLocalX, currentX, //
                        globalY, localY, totalY, currentGlobalY, currentLocalY, currentY, //
                        globalZ, localZ, totalZ, currentGlobalZ, currentLocalZ, currentZ);

                this.method.invoke(obj, param);

            }
            catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
    }

    void await() {
        if (exec.isQuiescent()) {
            return;
        }

        if (exec.isShutdown()) {
            return;
        }

        exec.awaitQuiescence(Integer.MAX_VALUE, TimeUnit.DAYS); // Wait forever
    }

    @Override
    public String getName() { return this.method.getName(); }

}
