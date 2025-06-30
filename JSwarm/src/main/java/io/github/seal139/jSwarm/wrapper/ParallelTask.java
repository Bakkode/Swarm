package io.github.seal139.jSwarm.wrapper;

import java.io.Closeable;
import java.io.IOException;

import io.github.seal139.jSwarm.backend.BackendException;
import io.github.seal139.jSwarm.backend.Context;
import io.github.seal139.jSwarm.backend.Executor;
import io.github.seal139.jSwarm.backend.Kernel;
import io.github.seal139.jSwarm.backend.Module;
import io.github.seal139.jSwarm.backend.Platform;
import io.github.seal139.jSwarm.backend.cuda.Cuda;
import io.github.seal139.jSwarm.backend.jvm.Jvm;
import io.github.seal139.jSwarm.backend.ocl.Ocl;
import io.github.seal139.jSwarm.datatype.Vector;
import io.github.seal139.jSwarm.misc.Common;
import io.github.seal139.jSwarm.misc.NativeCleaner.DeallocatedException;
import io.github.seal139.jSwarm.runtime.NdRange;
import io.github.seal139.jSwarm.runtime.Program;
import io.github.seal139.jSwarm.runtime.SyncDirection;

public class ParallelTask {

    // /*
    // * ParallelTask
    // * .from(Class<? extends Program> kernelCode)
    // *
    // * // Will handle load balancer in the future
    // * .atPlatform(Platform platform)
    // * or
    // * .atAnyPlatform()
    // * =================
    // *
    // * .withData(Vector<? extends Number> ... vars)
    // * .execute(String kernelName, NdRange range)
    // * .fetchData()
    // */

    // =================

    public static ProgramLoader from(Class<? extends Program> kernelCode) {
        return new ProgramLoader(kernelCode);
    }

    public static class ProgramLoader {

        private final Class<? extends Program> clazz;

        private static final Platform platform;

        static {
            Platform p = Jvm.getInstance();
            try {
                p = Ocl.getInstance();
                p = Cuda.getInstance();
            }
            catch (Throwable e) {
            }

            platform = p;
        }

        ProgramLoader(Class<? extends Program> clazz) {
            this.clazz = clazz;
        }

        public GeneralContext atPlatform(Platform platformTarget) throws BackendException, DeallocatedException {
            if (Common.isDebugMode()) {
                System.out.println("Debug mode detected. Using JVM as platform");
            }

            Platform p = Common.isDebugMode() ? Jvm.getInstance() : platformTarget;

            return new GeneralContext(p, this.clazz);
        }

        public GeneralContext atAnyPlatform() throws BackendException, DeallocatedException {
            return atPlatform(platform);
        }
    }

    public static class GeneralContext implements Closeable {

        private final Platform platform;
        private final Executor device;
        private final Context  ctx;
        private final Module   module;

        GeneralContext(Platform platform, Class<? extends Program> clazz) throws BackendException, DeallocatedException {
            this.platform = platform;
            this.device   = platform.getDevices()[0];
            this.ctx      = this.device.getDefaultContext();

            this.ctx.activate();

            this.module = this.ctx.loadProgram(clazz);
        }

        private Vector<? extends Number>[] vars = null;

        /**
         * Set data arguments before executing kernel
         *
         * @param vars
         * @return
         * @throws BackendException
         * @throws DeallocatedException
         */
        @SuppressWarnings("unchecked")
        public GeneralContext withArguments(Vector<? extends Number>... vars) throws BackendException, DeallocatedException {
            if (this.vars != null) {
                for (Vector<? extends Number> var : this.vars) {
                    this.ctx.unhook(var);
                }
            }

            this.vars = vars;

            for (Vector<? extends Number> var : vars) {
                this.ctx.hook(var);
            }

            this.ctx.sync(SyncDirection.TO_DEVICE, this.vars);
            this.ctx.waitOperation();

            return this;
        }

        /**
         * Synchronize memory back to host after processed in device
         *
         * @return {@link ParallelTask.GeneralContext} for continuous execution flow
         *
         * @throws BackendException
         * @throws DeallocatedException
         */
        public GeneralContext fetchData() throws BackendException, DeallocatedException {
            this.ctx.sync(SyncDirection.TO_HOST, this.vars);
            this.ctx.waitOperation();

            return this;
        }

        /**
         * Execute kernel function
         *
         * @param kernel Kernel function name
         * @param range  ND Range used
         * @return {@link ParallelTask.GeneralContext} for continuous execution flow
         *
         * @throws BackendException
         * @throws DeallocatedException
         */
        public GeneralContext execute(String kernel, NdRange range) throws BackendException, DeallocatedException {
            Kernel addKernel = this.module.getKernel(kernel);

            this.ctx.launch(addKernel, range, this.vars);
            return this;
        }

        public Platform getPlatform() { return this.platform; }

        public Executor getDevice() { return this.device; }

        public Context getCtx() { return this.ctx; }

        public Module getModule() { return this.module; }

        @Override
        public void close() throws IOException {
            try {
                if (this.vars != null) {
                    for (Vector<? extends Number> var : this.vars) {
                        this.ctx.unhook(var);
                    }
                }

                this.module.close();
            }
            catch (Exception e) {
                throw new IOException(e);
            }
        }
    }

}
