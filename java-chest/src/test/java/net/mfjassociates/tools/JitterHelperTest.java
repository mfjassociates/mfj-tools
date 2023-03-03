package net.mfjassociates.tools;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

public class JitterHelperTest {

	private static ExecutorService executor=Executors.newSingleThreadExecutor();
	private static ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);

	@Test
	public void test() throws InterruptedException, ExecutionException {
		Future<?> jitter = executor.submit(new JitterHelper());
		ScheduledFuture<?> result = scheduledExecutor.schedule(() -> {jitter.cancel(true);}, 3*5+1, TimeUnit.SECONDS);
		result.get();
	}

}
