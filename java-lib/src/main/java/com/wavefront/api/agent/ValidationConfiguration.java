package com.wavefront.api.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.annotation.Nullable;

/**
 * Data validation settings. Retrieved by the proxy from the back-end during check-in process.
 *
 * @author vasily@wavefront.com
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationConfiguration {

  /**
   * Maximum allowed metric name length. Default: 1023 characters (historic limit).
   */
  private int metricLengthLimit = 1023;

  /**
   * Maximum allowed histogram metric name length. Default: 1023 characters (historic limit).
   */
  private int histogramLengthLimit = 1023;

  /**
   * Maximum allowed span name length. Default: 128 characters.
   */
  private int spanLengthLimit = 128;

  /**
   * Maximum allowed log message length. Default: 20_000 characters (roughly 100 lines).
   */
  private int logLengthLimit = 20_000;

  /**
   * Maximum allowed host/source name length. Default: 1023 characters (historic limit).
   */
  private int hostLengthLimit = 1023;

  /**
   * Maximum allowed number of point tags per point/histogram. Default: 100 (historic limit).
   */
  private int annotationsCountLimit = 100;

  /**
   * Maximum allowed length for point tag keys. Enforced in addition to the 255 character limit
   * for 'key + "=" + value'. Default: 253 characters (historic limit).
   */
  private int annotationsKeyLengthLimit = 253;

  /**
   * Maximum allowed length for point tag values. Enforced in addition to the 255 character limit
   * for 'key + "=" + value'. Default: 255 characters.
   */
  private int annotationsValueLengthLimit = 255;

  /**
   * Maximum allowed number of annotations per span. Default: 20.
   */
  private int spanAnnotationsCountLimit = 20;

  /**
   * Maximum allowed length for span annotation keys. Enforced in addition to 255 character limit
   * for 'key + "=" + value'. Default: 128 characters.
   */
  private int spanAnnotationsKeyLengthLimit = 128;

  /**
   * Maximum allowed length for span annotation values. Enforced in addition to 255 character limit
   * for 'key + "=" + value'. Default: 128 characters.
   */
  private int spanAnnotationsValueLengthLimit = 128;

  /**
   * Maximum allowed length for log annotation keys. Default: 128 characters.
   */
  private int logAnnotationsKeyLengthLimit = 128;

  /**
   * Maximum allowed length for log annotation values. Default: 128 characters.
   */
  private int logAnnotationsValueLengthLimit = 128;

  /**
   * Maximum allowed number of log tags per log line. Default: 100
   */
  private int logAnnotationsCountLimit = 100;

  /**
   * This flag indicates if the customer is on CSP and tenants are converged (the same)
   * between WF and VRLIC
   */
  private boolean enableHyperlogsConvergedCsp = false;

  public int getMetricLengthLimit() {
    return metricLengthLimit;
  }

  public int getHistogramLengthLimit() {
    return histogramLengthLimit;
  }

  public int getSpanLengthLimit() {
    return spanLengthLimit;
  }

  public int getHostLengthLimit() {
    return hostLengthLimit;
  }

  public int getLogLengthLimit() {
    return logLengthLimit;
  }

  public int getAnnotationsCountLimit() {
    return annotationsCountLimit;
  }

  public int getAnnotationsKeyLengthLimit() {
    return annotationsKeyLengthLimit;
  }

  public int getAnnotationsValueLengthLimit() {
    return annotationsValueLengthLimit;
  }

  public int getSpanAnnotationsCountLimit() {
    return spanAnnotationsCountLimit;
  }

  public int getSpanAnnotationsKeyLengthLimit() {
    return spanAnnotationsKeyLengthLimit;
  }

  public int getSpanAnnotationsValueLengthLimit() {
    return spanAnnotationsValueLengthLimit;
  }

  public int getLogAnnotationsKeyLengthLimit() { return logAnnotationsKeyLengthLimit; }

  public int getLogAnnotationsValueLengthLimit() {
    return logAnnotationsValueLengthLimit;
  }

  public int getLogAnnotationsCountLimit() {
    return logAnnotationsCountLimit;
  }

  public ValidationConfiguration setMetricLengthLimit(int value) {
    this.metricLengthLimit = value;
    return this;
  }

  public ValidationConfiguration setHistogramLengthLimit(int value) {
    this.histogramLengthLimit = value;
    return this;
  }

  public ValidationConfiguration setSpanLengthLimit(int value) {
    this.spanLengthLimit = value;
    return this;
  }

  public ValidationConfiguration setHostLengthLimit(int value) {
    this.hostLengthLimit = value;
    return this;
  }

  public ValidationConfiguration setAnnotationsKeyLengthLimit(int value) {
    this.annotationsKeyLengthLimit = value;
    return this;
  }

  public ValidationConfiguration setAnnotationsValueLengthLimit(int value) {
    this.annotationsValueLengthLimit = value;
    return this;
  }

  public ValidationConfiguration setAnnotationsCountLimit(int value) {
    this.annotationsCountLimit = value;
    return this;
  }

  public ValidationConfiguration setSpanAnnotationsKeyLengthLimit(int value) {
    this.spanAnnotationsKeyLengthLimit = value;
    return this;
  }

  public ValidationConfiguration setSpanAnnotationsValueLengthLimit(int value) {
    this.spanAnnotationsValueLengthLimit = value;
    return this;
  }

  public ValidationConfiguration setSpanAnnotationsCountLimit(int value) {
    this.spanAnnotationsCountLimit = value;
    return this;
  }

  public ValidationConfiguration setLogLengthLimit(int value) {
    this.logLengthLimit = value;
    return this;
  }

  public ValidationConfiguration setLogAnnotationsKeyLengthLimit(int value) {
    this.logAnnotationsKeyLengthLimit = value;
    return this;
  }

  public ValidationConfiguration setLogAnnotationsValueLengthLimit(int value) {
    this.logAnnotationsValueLengthLimit = value;
    return this;
  }

  public ValidationConfiguration setLogAnnotationsCountLimit(int value) {
    this.logAnnotationsCountLimit = value;
    return this;
  }

  public boolean enableHyperlogsConvergedCsp() {
    return enableHyperlogsConvergedCsp;
  }

  public ValidationConfiguration setEnableHyperlogsConvergedCsp(boolean enableHyperlogsConvergedCsp) {
    this.enableHyperlogsConvergedCsp = enableHyperlogsConvergedCsp;
    return this;
  }

  public void updateFrom(@Nullable ValidationConfiguration other) {
    if (other == null) return;
    this.metricLengthLimit = other.getMetricLengthLimit();
    this.histogramLengthLimit = other.getHistogramLengthLimit();
    this.spanLengthLimit = other.getSpanLengthLimit();
    this.logLengthLimit = other.getLogLengthLimit();
    this.hostLengthLimit = other.getHostLengthLimit();
    this.annotationsCountLimit = other.getAnnotationsCountLimit();
    this.annotationsKeyLengthLimit = other.getAnnotationsKeyLengthLimit();
    this.annotationsValueLengthLimit = other.getAnnotationsValueLengthLimit();
    this.spanAnnotationsCountLimit = other.getSpanAnnotationsCountLimit();
    this.spanAnnotationsKeyLengthLimit = other.getSpanAnnotationsKeyLengthLimit();
    this.spanAnnotationsValueLengthLimit = other.getSpanAnnotationsValueLengthLimit();
    this.logAnnotationsKeyLengthLimit = other.getLogAnnotationsKeyLengthLimit();
    this.logAnnotationsValueLengthLimit = other.getLogAnnotationsValueLengthLimit();
    this.logAnnotationsCountLimit = other.getLogAnnotationsCountLimit();
    this.enableHyperlogsConvergedCsp = other.enableHyperlogsConvergedCsp();
  }
}
