package com.wavefront.integrations.metrics;

import com.google.common.annotations.VisibleForTesting;
import com.wavefront.common.MetricsToTimeseries;
import com.wavefront.common.Pair;
import com.wavefront.metrics.MetricTranslator;
import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.SafeVirtualMachineMetrics;
import com.yammer.metrics.core.VirtualMachineMetrics;
import com.yammer.metrics.core.WavefrontHistogram;
import com.yammer.metrics.reporting.AbstractReporter;
import org.apache.commons.lang.StringUtils;
import org.apache.http.nio.reactor.IOReactorException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Uses HTTP instead of Sockets to connect and send metrics to a wavefront proxy
 *
 * @author Mike McMahon (mike@wavefront.com)
 */
public class WavefrontYammerHttpMetricsReporter extends AbstractReporter implements Runnable {

  protected static final Logger logger = Logger.getLogger(WavefrontYammerHttpMetricsReporter.class.getCanonicalName());

  private static final Clock clock = Clock.defaultClock();
  private static final VirtualMachineMetrics vm = SafeVirtualMachineMetrics.getInstance();
  private final ScheduledExecutorService executor;

  private final boolean includeJvmMetrics;
  private final ConcurrentHashMap<String, Double> gaugeMap;
  private final MetricTranslator metricTranslator;
  private final HttpMetricsProcessor httpMetricsProcessor;

  /**
   * How many metrics were emitted in the last call to run()
   */
  private AtomicInteger metricsGeneratedLastPass = new AtomicInteger();

  public static class Builder {
    private MetricsRegistry metricsRegistry;
    private String name;
    private String defaultSource;

    // Primary
    private String hostname;
    private int port;

    // Secondary
    private String secondaryHostname;
    private int secondaryPort;

    private Supplier<Long> timeSupplier = System::currentTimeMillis;
    private boolean prependGroupName = false;
    private MetricTranslator metricTranslator = null;
    private boolean includeJvmMetrics = false;
    private boolean sendZeroCounters = false;
    private boolean sendEmptyHistograms = false;
    private boolean clear = false;
    private int queueSize = 50_000;
    private int batchSize = 10_000;

    public Builder withMetricsRegistry(MetricsRegistry metricsRegistry) {
      this.metricsRegistry = metricsRegistry;
      return this;
    }

    public Builder withName(String name) {
      this.name = name;
      return this;
    }

    public Builder withDefaultSource(String defaultSource) {
      this.defaultSource = defaultSource;
      return this;
    }

    public Builder withTimeSupplier(Supplier<Long> timeSupplier) {
      this.timeSupplier = timeSupplier;
      return this;
    }

    public Builder withEndpoint(String hostname, int port) {
      this.hostname = hostname;
      this.port = port;
      return this;
    }

    public Builder withSecondaryEndpoint(String hostname, int port) {
      this.secondaryHostname = hostname;
      this.secondaryPort = port;
      return this;
    }

    public Builder withQueueOptions(int batchSize, int queueSize) {
      this.batchSize = batchSize;
      this.queueSize = queueSize;
      return this;
    }

    public Builder withPrependedGroupNames(boolean prependGroupName) {
      this.prependGroupName = prependGroupName;
      return this;
    }

    public Builder clearHistogramsAndTimers(boolean clear) {
      this.clear = clear;
      return this;
    }

    public Builder sendZeroCounters(boolean send) {
      this.sendZeroCounters = send;
      return this;
    }

    public Builder sendEmptyHistograms(boolean send) {
      this.sendEmptyHistograms = send;
      return this;
    }

    public Builder includeJvmMetrics(boolean include) {
      this.includeJvmMetrics = include;
      return this;
    }

    public Builder withMetricTranslator(MetricTranslator metricTranslator) {
      this.metricTranslator = metricTranslator;
      return this;
    }

    public WavefrontYammerHttpMetricsReporter build() throws IOReactorException {
      if (StringUtils.isBlank(this.name)) {
        throw new IllegalArgumentException("Reporter must have a human readable name.");
      }
      if (StringUtils.isBlank(this.hostname)) {
        throw new IllegalArgumentException("Hostname may not be blank.");
      }
      return new WavefrontYammerHttpMetricsReporter(this);
    }

  }

  private WavefrontYammerHttpMetricsReporter(Builder builder) throws IOReactorException {
    super(builder.metricsRegistry);
    this.executor = builder.metricsRegistry.newScheduledThreadPool(1, builder.name);
    this.metricTranslator = builder.metricTranslator;
    this.includeJvmMetrics = builder.includeJvmMetrics;
    HttpMetricsProcessor.Builder httpMetricsProcessorBuilder = new HttpMetricsProcessor.Builder().
        withEndpoint(builder.hostname,builder.port).
        withPrependedGroupNames(builder.prependGroupName).
        withQueueOptions(builder.batchSize,builder.queueSize).
        sendEmptyHistograms(builder.sendEmptyHistograms).
        clearHistogramsAndTimers(builder.clear).
        withTimeSupplier(builder.timeSupplier).
        withDefaultSource(builder.defaultSource).
        sendZeroCounters(builder.sendZeroCounters);
    if (builder.secondaryHostname != null) {
      httpMetricsProcessorBuilder.withSecondaryEndpoint(builder.secondaryHostname,builder.secondaryPort);
    }
    this.httpMetricsProcessor = httpMetricsProcessorBuilder.build();
    this.gaugeMap = new ConcurrentHashMap<>();
  }

  private void upsertGauges(String metricName, Double t) {
    gaugeMap.put(metricName, t);

    // This call to newGauge only mutates the metrics registry the first time through. Thats why it's important
    // to access gaugeMap indirectly, as opposed to counting on new calls to newGauage to replace the underlying
    // double supplier.
    getMetricsRegistry().newGauge(
        new MetricName("", "", MetricsToTimeseries.sanitize(metricName)),
        new Gauge<Double>() {
          @Override
          public Double value() {
            return gaugeMap.get(metricName);
          }
        });
  }

  private void upsertGauges(String base, Map<String, Double> metrics) {
    for (Map.Entry<String, Double> entry : metrics.entrySet()) {
      upsertGauges(base + "." + entry.getKey(), entry.getValue());
    }
  }

  private void upsertJavaMetrics() {
    upsertGauges("jvm.memory", MetricsToTimeseries.memoryMetrics(vm));
    upsertGauges("jvm.buffers.direct", MetricsToTimeseries.buffersMetrics(vm.getBufferPoolStats().get("direct")));
    upsertGauges("jvm.buffers.mapped", MetricsToTimeseries.buffersMetrics(vm.getBufferPoolStats().get("mapped")));
    upsertGauges("jvm.thread-states", MetricsToTimeseries.threadStateMetrics(vm));
    upsertGauges("jvm", MetricsToTimeseries.vmMetrics(vm));
    upsertGauges("current_time", (double) clock.time());
    for (Map.Entry<String, VirtualMachineMetrics.GarbageCollectorStats> entry : vm.garbageCollectors().entrySet()) {
      upsertGauges("jvm.garbage-collectors." + entry.getKey(), MetricsToTimeseries.gcMetrics(entry.getValue()));
    }
  }

  /**
   * @return How many metrics were processed during the last call to {@link #run()}.
   */
  @VisibleForTesting
  int getMetricsGeneratedLastPass() {
    return metricsGeneratedLastPass.get();
  }

  /**
   * Starts the reporter polling at the given period.
   *
   * @param period the amount of time between polls
   * @param unit   the unit for {@code period}
   */
  public void start(long period, TimeUnit unit) {
    executor.scheduleAtFixedRate(this, period, period, unit);
  }

  /**
   * Starts the reporter polling at the given period with specified initial delay
   *
   * @param initialDelay the amount of time before first execution
   * @param period       the amount of time between polls
   * @param unit         the unit for {@code initialDelay} and {@code period}
   */
  public void start(long initialDelay, long period, TimeUnit unit) {
    executor.scheduleAtFixedRate(this, initialDelay, period, unit);
  }

  /**
   * Shuts down the reporter polling, waiting the specific amount of time for any current polls to
   * complete.
   *
   * @param timeout the maximum time to wait
   * @param unit    the unit for {@code timeout}
   * @throws InterruptedException if interrupted while waiting
   */
  public void shutdown(long timeout, TimeUnit unit) throws InterruptedException {
    executor.shutdown();
    executor.awaitTermination(timeout, unit);
    super.shutdown();
  }

  @Override
  public void shutdown() {
    executor.shutdown();
    super.shutdown();
  }

  @Override
  public void run() {
    metricsGeneratedLastPass.set(0);
    try {
      if (includeJvmMetrics) upsertJavaMetrics();
      // non-histograms go first
      getMetricsRegistry().allMetrics().entrySet().stream().filter(m -> !(m.getValue() instanceof WavefrontHistogram)).
          forEach(this::processEntry);
      // histograms go last
      getMetricsRegistry().allMetrics().entrySet().stream().filter(m -> m.getValue() instanceof WavefrontHistogram).
          forEach(this::processEntry);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Cannot report point to Wavefront! Trying again next iteration.", e);
    }
  }

  private void processEntry(Map.Entry<MetricName, Metric> entry) {
    try {
      MetricName metricName = entry.getKey();
      Metric metric = entry.getValue();
      if (metricTranslator != null) {
        Pair<MetricName, Metric> pair = metricTranslator.apply(Pair.of(metricName, metric));
        if (pair == null) return;
        metricName = pair._1;
        metric = pair._2;
      }
      metric.processWith(httpMetricsProcessor, metricName, null);
      metricsGeneratedLastPass.incrementAndGet();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Force metrics enqueued for sending to be flushed immediately
   */
  public void flush() {
    this.httpMetricsProcessor.flush();
  }
}
