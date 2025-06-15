package io.github.seal139.jSwarm.backend.jvm;

import io.github.seal139.jSwarm.backend.Executor;
import io.github.seal139.jSwarm.backend.Platform;
import io.github.seal139.jSwarm.backend.cuda.CudaException;

public class Jvm implements Platform {

    private static Jvm obj;

    public static Jvm getInstance() throws Error, CudaException {
        if (obj == null) {
            obj = new Jvm();
        }

        return obj;
    }

    // ===================================

    @Override
    public String getName() { return "JVM"; }

    @Override
    public String getFullName() { // TODO Auto-generated method stub
        return "Java Virtual Machine";
    }

    @Override
    public boolean isPrimary() { // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getVersion() { // TODO Auto-generated method stub
        return "1.0";
    }

    private final JvmDevice device = new JvmDevice();

    @Override
    public Executor[] getDevices() { // TODO Auto-generated method stub
        return new Executor[] {
                this.device };
    }

}
