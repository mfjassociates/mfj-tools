package net.mfjassociates.tools;

import java.text.MessageFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class JitterHelper implements Runnable {

	private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	private static CompletableFuture<Long> cf = new CompletableFuture<Long>();
	private static Runnable task2 = () -> {
		long l=System.nanoTime();
		cf.complete(l);
	};
	private static final int DELAY = 3;
	private static final long DELAY_NANO = DELAY * 1000000000l;
	private final DescriptiveStatistics stats=new DescriptiveStatistics();
	public static void main(String[] args) {
		JitterHelper jh=new JitterHelper();
		jh.run();
	}
	private static final String[] UNITS = new String[] {"nano","micro","milli","second"};
	private static final long THRESHOLD = 1000000; // threshold to display data in nanoseconds (1 millisecond or more)
	private static String convert(long diff) {
		float lasti=1;
		int unitI=-1;
		for (long i = 1l; i < Math.abs(diff); i=i*1000) {
			lasti=i;
			unitI++;
		}
		if (unitI==-1) unitI=0; // if diff was 0, make sure it is in nano
		Float fdiff=diff/lasti;
		return fdiff.toString()+" "+UNITS[unitI];
	}
	private long samples=0;
	@Override
	public void run() {
		try {
			Runtime.getRuntime().addShutdownHook(new Thread( () -> {displayFinalStats();}));
			executor.scheduleAtFixedRate(task2, 3, 3, TimeUnit.SECONDS);
			long p=System.nanoTime();
			long c;
			long diff;
			while (true) {
				c=cf.get();
				samples++;
				diff=DELAY_NANO - (c-p);
				stats.addValue(diff);
				cf=new CompletableFuture<Long>();
				double mean = stats.getMean();
				double std = stats.getStandardDeviation();
				float nstd=1.0f;
				double tolerance=nstd*std;
				double delta=diff-mean;
//				if (diff<(mean-tolerance) || diff > (mean+tolerance))
//				System.out.println(diff);
				if (Math.abs(diff)>THRESHOLD)
				System.out.println(MessageFormat.format("{6}: \"{0}\", Difference between ticks= ,\"{0}\", mean= ,\"{1}\", variance= ,\"{2}\", min= ,\"{3}\", max= ,\"{4}\", std= ,\"{5}\"", diff, mean, stats.getVariance(), stats.getMin(), stats.getMax(), stats.getStandardDeviation(), new Date()));
				p=c;
			}
		} catch (InterruptedException | ExecutionException e) {
			displayFinalStats();
		}
	}
	private void displayFinalStats() {
		System.out.println(MessageFormat.format("Stats: samples={5}, mean={0}, variance={1}, min={2}, max={3}, std={4}", stats.getMean(), stats.getVariance(), stats.getMin(), stats.getMax(), stats.getStandardDeviation(), samples));
	}
}
