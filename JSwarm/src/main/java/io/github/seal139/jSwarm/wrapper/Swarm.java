package io.github.seal139.jSwarm.wrapper;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.seal139.jSwarm.backend.cuda.Cuda;
import io.github.seal139.jSwarm.core.Executor;
import io.github.seal139.jSwarm.core.Platform;
import io.github.seal139.jSwarm.misc.Common;

public final class Swarm {
    private Swarm() {
    }

    private static final Set<Class<? extends Platform>> platformCls = new HashSet<>();

    static {
        platformCls.add(Cuda.class); // CUDA
//		platformCls.add(HipAmd.class);	// HIP - AMD
//		platformCls.add(HipNv.class);	// HIP - Nvidia
//		platformCls.add(OpenCL.class);	// OpenCL
//		platformCls.add(Jvm.class);		// JVM
    }

    private static Map<String, Platform> platforms = new HashMap<>();

    private static <T extends Platform> void load(Class<T> cls) {
        try {
            Constructor<T> constructor = cls.getDeclaredConstructor();
            constructor.setAccessible(true);

            T obj = constructor.newInstance();

            platforms.put(obj.getName(), obj);
        }
        catch (Throwable e) {
        }
    }

    /**
     * Register new additional {@link Platform} driver to Swarm. Additional driver
     * should be registered before {@link Swarm#init()} is called.
     *
     * @param driver {@link Platform} driver class.
     */
    public static void register(Class<? extends Platform> driver) {
        platformCls.add(driver);
    }

    /**
     * Initialize platform driver. This method can only be called once. Attempting
     * to call more than once will do nothing. Make sure your additional driver is
     * registered through {@link Swarm#register(Class)} before calling this method.
     *
     * @throws Exception
     */
    public static void init() throws Exception {
        for (Class<? extends Platform> plCls : platformCls) {
            Common.queue(() -> {
                load(plCls);
                return null;
            });
        }

        Common.await("driver-load", /* wait for */ 1 /* minute */);
    }

    /**
     * Is this program runs in debug mode? <br/>
     * This is an alternative method of {@link Common#isDebugMode()}.
     *
     * @return True only when debug.
     */
    public static boolean isDebugMode() { return Common.isDebugMode(); }

    private static boolean allowDebugKernel = true;

    /**
     * Allow debug and set breakpoint on kernel code. By default, this behavior is
     * always true <br/>
     * <br/>
     *
     * Notes: <br/>
     * Kernel cannot be debugged when run through parallel API. To allow that, we
     * need to change executor to JVM (With reduced performance). <b> Only apply
     * when {@link Swarm#isDebugMode()} is true.</br>
     *
     * @param flag Set true to allow debug on kernel
     */
    public static void debugKernel(boolean flag) {
        allowDebugKernel = flag;
    }

    /**
     * Get operating system used to run this program. <br/>
     * This is an alternative method of {@link Common#getOs()}.
     *
     * @return Operating system name.
     */
    public static String getOperatingSystem() { return Common.getOs(); }

    /**
     * Get {@link Platform} name supported to be used.
     *
     * @return Array of {@link Platform} name. Sorted by most preferred.
     */
    public static String[] getPlatformNames() {
        String[] strs = new String[platforms.size()];

        int i = -1;
        for (String name : platforms.keySet()) {
            strs[++i] = name;
        }

        return strs;
    }

    /**
     * Get all {@link Executor} from all {@link Platform}.
     *
     * @return Array of {@link Executor}. Guaranteed to be sorted based on
     *         {@link Platform#isPrimary()} and device performance index.
     */
    public static Executor[] getDevices() {
        if (isDebugMode() && allowDebugKernel) {
            return getDevices("jvm");
        }

        return null;
    }

    /**
     * Get all {@link Executor} from specific {@link Platform} by it's name.
     *
     * @param platformName Name of the platform. Can be retrieved from
     *                     {@link Swarm#getPlatformNames()}.
     * @return Array of {@link Executor}. Guaranteed to be sorted based on
     *         performance index.
     */
    public static Executor[] getDevices(String platformName) {
        Platform driver = platforms.get(platformName);
        if (driver == null) {
            // throw new IOException("Driver not found for " + platformName);
        }

        return driver.getDevices();
    }

    /**
     * Get the highest performance {@link Executor} from all {@link Platform}.
     *
     * @return Highest performance {@link Executor}.
     */
    public static Executor getDevice() {
        if (isDebugMode() && allowDebugKernel) {
            return getDevices("jvm")[0];
        }

        return getDevices()[0];
    }

    /**
     * Get the highest performance {@link Executor} from specific {@link Platform}.
     *
     * @param platformName Name of the platform. Can be retrieved from
     *                     {@link Swarm#getPlatformNames()}.
     * @return Highest performance {@link Executor}.
     */
    public static Executor getDevice(String platformName) {
        if (isDebugMode() && allowDebugKernel) {
            return getDevices("jvm")[0];
        }

        return getDevices(platformName)[0];
    }
}
