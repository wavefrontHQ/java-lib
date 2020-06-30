package com.wavefront.ingester;

import java.util.function.Function;

import com.google.common.annotations.VisibleForTesting;

import wavefront.report.ReportMetric;

import static com.wavefront.common.SerializerUtils.appendQuoted;
import static com.wavefront.common.SerializerUtils.appendTagMap;

/**
 * Convert a {@link ReportMetric} to its string representation in a canonical format
 * (quoted metric name, tag values and keys (except for "source").
 *
 * @author vasily@wavefront.com
 */
public class ReportMetricSerializer implements Function<ReportMetric, String> {

  @Override
  public String apply(ReportMetric point) {
    return metricToString(point);
  }

  @VisibleForTesting
  public static String metricToString(ReportMetric point) {
    StringBuilder sb = new StringBuilder(80);
    appendQuoted(sb, point.getMetric()).
        append(" ").append(point.getValue()).
        append(" ").append(point.getTimestamp() / 1000).
        append(" ").append("source=");
    appendQuoted(sb, point.getHost());
    appendTagMap(sb, point.getAnnotations());
    return sb.toString();
  }
}
