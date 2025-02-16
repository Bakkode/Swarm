package io.github.seal139.jSwarm.misc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Log {

    public static void error(Throwable e) {

    }

    private static void diag(String threadName, String operation, String message) {

    }

    private static final ExecutorService logger = Executors.newSingleThreadExecutor();

    public static void diagnostic(String operation, String message) {
        String thread = Thread.currentThread().getName();
        logger.submit(() -> diag(thread, operation, message));
    }
}
