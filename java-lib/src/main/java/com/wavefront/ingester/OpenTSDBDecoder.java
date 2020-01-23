package com.wavefront.ingester;

import com.google.common.base.Preconditions;

import java.util.List;

import com.google.common.collect.ImmutableList;
import wavefront.report.ReportPoint;

/**
 * OpenTSDB decoder that takes in a point of the type:
 *
 * PUT [metric] [timestamp] [value] [annotations]
 *
 * @author Clement Pang (clement@wavefront.com).
 */
public class OpenTSDBDecoder implements ReportableEntityDecoder<String, ReportPoint> {

  private static final AbstractIngesterFormatter<ReportPoint> FORMAT =
      ReportPointIngesterFormatter.newBuilder().
          caseInsensitiveLiterals(ImmutableList.of("put")).
          text(ReportPoint::setMetric).
          timestamp(ReportPoint::setTimestamp).
          value(ReportPoint::setValue).
          annotationMap(ReportPoint::setAnnotations).
          build();
  private final String hostName;
  private List<String> customSourceTags;

  public OpenTSDBDecoder(String hostName, List<String> customSourceTags) {
    Preconditions.checkNotNull(hostName);
    Preconditions.checkNotNull(customSourceTags);
    this.hostName = hostName;
    this.customSourceTags = customSourceTags;
  }

  @Override
  public void decode(String msg, List<ReportPoint> out, String customerId) {
    ReportPoint point = FORMAT.drive(msg, () -> hostName, customerId, customSourceTags);
    if (out != null) {
      out.add(point);
    }
  }
}
