package com.wavefront.ingester;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.time.DateUtils;
import wavefront.report.Histogram;
import wavefront.report.HistogramType;
import wavefront.report.ReportHistogram;

/**
 * Decoder that takes in histograms of the type:
 *
 * [BinType] [Timestamp] [Centroids] [Metric] [Annotations]
 *
 * @author Tim Schmidt (tim@wavefront.com).
 */
public class ReportHistogramDecoder implements ReportableEntityDecoder<String, ReportHistogram> {

  private static final AbstractIngesterFormatter<ReportHistogram> FORMAT =
      ReportHistogramIngesterFormatter.newBuilder().
          caseSensitiveLiterals(ImmutableList.of("!M", "!H", "!D"),
              ReportHistogramDecoder::setBinType).
          optionalTimestamp(ReportHistogram::setTimestamp).
          centroids().
          text(ReportHistogram::setMetric).
          annotationMap(ReportHistogram::setAnnotations).
          build();

  private final Supplier<String> defaultHostNameSupplier;

  public ReportHistogramDecoder() {
    this("unknown");
  }

  public ReportHistogramDecoder(String defaultHostName) {
    this(() -> defaultHostName);
  }

  public ReportHistogramDecoder(Supplier<String> defaultHostNameSupplier) {
    this.defaultHostNameSupplier = defaultHostNameSupplier;
  }

  @Override
  public void decode(String msg, List<ReportHistogram> out, String customerId,
                     IngesterContext ctx) {
    ReportHistogram point = FORMAT.drive(msg, defaultHostNameSupplier, customerId, null, ctx);
    if (point != null) {
      // adjust timestamp according to histogram bin first
      long duration = point.getValue().getDuration();
      point.setTimestamp((point.getTimestamp() / duration) * duration);
      out.add(ReportHistogram.newBuilder(point).build());
    }
  }

  @Override
  public void decode(String msg, List<ReportHistogram> out) {
    throw new UnsupportedOperationException("Customer ID extraction is not supported");
  }

  private static void setBinType(ReportHistogram target, String binType) {
    int durationMillis;
    switch (binType) {
      case "!M":
        durationMillis = (int) DateUtils.MILLIS_PER_MINUTE;
        break;
      case "!H":
        durationMillis = (int) DateUtils.MILLIS_PER_HOUR;
        break;
      case "!D":
        durationMillis = (int) DateUtils.MILLIS_PER_DAY;
        break;
      default:
        throw new RuntimeException("Unknown BinType " + binType);
    }
    Histogram histogram = new Histogram();
    histogram.setDuration(durationMillis);
    histogram.setType(HistogramType.TDIGEST);
    target.setValue(histogram);
  }
}
