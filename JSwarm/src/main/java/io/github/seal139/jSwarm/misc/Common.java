package io.github.seal139.jSwarm.misc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class Common {
    private static final ForkJoinPool w = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

    private static final String  osName;
    private static final boolean debug;

    static {
        /* Check OS */ {
            final String OS = System.getProperty("os.name").toLowerCase();
            if (OS.contains("win")) {
                osName = "Windows";
            }
            else if (OS.contains("mac")) {
                osName = "Mac";
            }
            else if (OS.contains("nix") || OS.contains("nux") || OS.contains("aix")) {
                osName = "Unix";
            }
            else {
                osName = "Unknown";
            }
        }

        /* Check debug mode */ {
            String[] arguments = ManagementFactory.getRuntimeMXBean().getInputArguments().toArray(new String[0]);
            boolean  d         = false;
            for (String arg : arguments) {
                if (d = arg.contains("jdwp") || arg.contains("-Xdebug")) {
                    break;
                }
            }

            debug = d;
        }
    }

    public static <T> Future<T> queue(Callable<T> task) {
        return w.submit(task);
    }

    public static void await(String job, int timeout) throws Exception {
        if (!w.awaitQuiescence(timeout, TimeUnit.MINUTES)) {
            throw new Exception("Failed to complete task for " + job);
        }
    }

    public static String getDriverByOs(String platform, String name) throws IOException {
        File f = File.createTempFile("swarm-", ".driver");
        f.deleteOnExit();

        try (InputStream inputStream = Common.class.getResourceAsStream("/" + getOs() + "/" + name)) {
            if (inputStream == null) {
                f.delete();
                throw new FileNotFoundException("No driver for " + platform + " found for this operating system");
            }

            Files.copy(inputStream, f.toPath());
        }

        return f.getAbsolutePath();
    }

    /**
     * Get operating system used to run this program
     *
     * @return Operating system name
     */
    public static String getOs() { return osName; }

    /**
     * Is this program runs in debug mode?
     * 
     * @return True only when debug
     */
    public static boolean isDebugMode() { return debug; }
}
