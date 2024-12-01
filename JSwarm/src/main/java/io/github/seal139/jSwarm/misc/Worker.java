package io.github.seal139.jSwarm.misc;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Most of the task is an expensive process. Do it in parallel so we can continue another important task in parallel.
 */
public final class Worker {
	private static final ForkJoinPool w = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
	
	public static <T> Future<T> queue(Callable<T> task) {
		return w.submit(task);
	}

	public static void await(String job, int timeout) throws Exception{
		if (!w.awaitQuiescence(timeout, TimeUnit.MINUTES)) {
			throw new Exception("Failed to complete task for " + job);
		}
	}
}
