package com.wavefront.ingester;

import java.util.HashMap;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import wavefront.report.ReportMetric;

/**
 * @author Andrew Kao (andrew@wavefront.com), Jason Bau (jbau@wavefront.com), vasily@wavefront.com
 */
public class ReportMetricSerializerTest {

  private final Function<ReportMetric, String> serializer = new ReportMetricSerializer();

  @Test
  public void testReportMetricToString() {
    // Common case.
    Assert.assertEquals("\"some metric\" 10.0 1469751813 source=\"host\" \"foo\"=\"bar\" " +
            "\"boo\"=\"baz\"",
        serializer.apply(new ReportMetric("some metric",1469751813000L, 10.0, "host", "table",
            ImmutableMap.of("foo", "bar", "boo", "baz"))));
    Assert.assertEquals("\"some metric\" 10.0 1469751813 source=\"host\"",
        serializer.apply(new ReportMetric("some metric",1469751813000L, 10.0, "host", "table",
            ImmutableMap.of())));

    // Quote in metric name
    Assert.assertEquals("\"some\\\"metric\" 10.0 1469751813 source=\"host\"",
        serializer.apply(new ReportMetric("some\"metric", 1469751813000L, 10.0, "host", "table",
            new HashMap<String, String>()))
    );
    // Quote in tags
    Assert.assertEquals("\"some metric\" 10.0 1469751813 source=\"host\" \"foo\\\"\"=\"\\\"bar\" " +
            "\"bo\\\"o\"=\"baz\"",
        serializer.apply(new ReportMetric("some metric", 1469751813000L, 10.0, "host", "table",
            ImmutableMap.of("foo\"", "\"bar", "bo\"o", "baz")))
    );
  }
}
