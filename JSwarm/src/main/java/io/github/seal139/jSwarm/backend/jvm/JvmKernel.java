package io.github.seal139.jSwarm.backend.jvm;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import io.github.seal139.jSwarm.backend.BackendException;
import io.github.seal139.jSwarm.backend.Kernel;
import io.github.seal139.jSwarm.misc.Common;
import io.github.seal139.jSwarm.runtime.NdRange;
import io.github.seal139.jSwarm.runtime.Program;
import io.github.seal139.jSwarm.runtime.WorkItemController;

public class JvmKernel implements Kernel {

    private final Supplier<Program> ctor;

    private final MethodHandle spreadInvoker;

    private final String name;

    final int             batch = Runtime.getRuntime().availableProcessors();
    final ExecutorService exec  = Executors.newFixedThreadPool(this.batch);

    final MethodType invokedType   = MethodType.methodType(Supplier.class);
    final MethodType samMethodType = MethodType.methodType(Object.class);

    JvmKernel(Constructor<?> ctor, Method method) {

        this.name = method.getName();

        MethodHandle hwnd = null;

        Supplier<Program> ivkr = null;

        try {
            final MethodHandles.Lookup lookup = MethodHandles.lookup();

            final MethodHandle cnstor = lookup.unreflectConstructor(ctor);
            final MethodHandle mh     = lookup.unreflect(method);

            hwnd = mh.asSpreader(Number[].class, method.getParameterCount()); //

            // Constructor fast-access
            CallSite site = LambdaMetafactory.metafactory(lookup, "get", //
                    this.invokedType, //
                    this.samMethodType, //
                    cnstor, //
                    cnstor.type() //
            );

            ivkr = (Supplier<Program>) site.getTarget().invokeExact();
        }
        catch (Throwable e) {
            e.printStackTrace();
        }

        this.ctor          = ivkr;
        this.spreadInvoker = hwnd;
    }

    CountDownLatch run(NdRange range, Number... param) throws BackendException {

        // To simulate local thread barrier efficiently, inverse the loop order. Loop
        // through global first, then the local. So we do not need local thread barrier
        // anymore
        // This also can improve performance by loop larger number through smaller
        // thread loop

        int sizeX      = range.getXLocal();
        int sizeY      = range.getYLocal();
        int sizeZ      = range.getZLocal();
        int totalItems = sizeX * sizeY * sizeZ;

        final int itemsPerBatch = ((totalItems + this.batch) - 1) / this.batch;

        final CountDownLatch latch = new CountDownLatch(this.batch);

//        final int endIndex = this.batch * itemsPerBatch;

        for (int start = 0; start < /* endIndex */ totalItems; start += itemsPerBatch) {

            final int _start = start;

            Common.queue(() -> {
                int end = Math.min(totalItems, _start + itemsPerBatch);

                try {
                    this.exec.execute(new WorkItemController(latch, this.ctor, this.spreadInvoker, param, range, _start, end));
                }
                catch (IllegalArgumentException e) {
                    throw new JvmException(e);
                }
                catch (Throwable e) {
                    e.printStackTrace();
                }
                return null;
            });

        }

        return latch;
    }

    @Override
    public String getName() { return this.name; }

}
