package com.wavefront.api.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Configuration for the SSH Daemon.
 *
 * @author Clement Pang (clement@sunnylabs.com)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AgentConfiguration {

  public String name;
  @Deprecated
  public String defaultUsername;
  @Deprecated
  public String defaultPublicKey;
  public boolean allowAnyHostKeys = true;
  public Long currentTime;
  private boolean collectorSetsPointsPerBatch = false;
  private Long pointsPerBatch;
  private boolean collectorSetsRetryBackoff = false;
  private Double retryBackoffBaseSeconds;
  private boolean collectorSetsRateLimit = false;
  private List<SpanSamplingPolicy> activeSpanSamplingPolicies;

  /**
   * When set, enforces a per proxy rate limit for points.
   */
  private Long collectorRateLimit;

  /**
   * When set, enforces a per proxy rate limit for histogram distributions.
   */
  private Long histogramRateLimit;

  /**
   * When set, enforces a per proxy rate limit for source tags.
   */
  private Double sourceTagsRateLimit;

  /**
   * When set, enforces a per proxy rate limit for spans.
   */
  private Long spanRateLimit;

  /**
   * When set, enforces a per proxy rate limit for span logs.
   */
  private Long spanLogsRateLimit;

  /**
   * When set, enforces a per proxy rate limit for events.
   */
  private Double eventsRateLimit;

  /**
   * When set, enforces a per proxy rate limit for logs.
   */
  private Long logsRateLimit;

  private boolean shutOffAgents = false;
  private boolean showTrialExpired = false;

  /**
  * If the value is true the metrics queue will be truncated
  */
  private boolean truncateQueue = false;

  /**
   * A custom message to display before shutting down the proxy
   */
  private String shutOffMessage;

  /**
   * If the value is true, then histogram feature is disabled
   */
  private boolean histogramDisabled = false;

  /**
   * If the value is true, then trace feature is disabled
   */
  private boolean traceDisabled = false;

  /**
   * If the value is true, then span logs are disabled
   */
  private boolean spanLogsDisabled = false;

  /**
   * If the value is true, then logs feature is disabled
   */
  private boolean logsDisabled = false;

  /**
   * This is the endpoint to use to talk to the log server
   */
  private String logServerEndpointUrl;

  /**
   * This is the token to use to talk to the log server
   */
  private String logServerToken;

  /**
   * This flag indicates if the customer is on CSP and tenants are converged (the same)
   * btw WF and VRLIC
   */
  private boolean enableHyperlogsConvergedCsp = false;

  /**
   * Server-side configuration for various limits to be enforced at the proxy.
   */
  private ValidationConfiguration validationConfiguration;

  /**
   * Global PPS limit (collectorPPS)
   */
  private Long globalCollectorRateLimit;

  /**
   * Global histogram DPS limit (collectorHistogramPPS).
   */
  private Long globalHistogramRateLimit;

  /**
   * Global rate limit for source tags operations (taggedSourceOPS)
   */
  private Double globalSourceTagRateLimit;

  /**
   * Global tracing span SPS limit (collectorTracingPPS)
   */
  private Long globalSpanRateLimit;

  /**
   * Global span logs logs/s limit (collectorSpanLogsPPS)
   */
  private Long globalSpanLogsRateLimit;

  /**
   * Global rate limit for new events (collectorEPS)
   */
  private Double globalEventRateLimit;

  /**
   * Global logs logs/s limit (collectorLogsPPS)
   */
  private Long globalLogsRateLimit;

  /**
   * Controls the number of centroids in a histogram.
   */
  private Integer histogramStorageAccuracy;

  /**
   * Yaml string with preprocessor rules to override local rules at the proxy.
   */
  private String preprocessorRules;

  /**
   * System metric whitelist.
   */
  private List<String> systemMetrics;

  /**
   * Sampling rate to enforce for spans. This value ranges between 0.0 to 1.0.
   */
  private Double spanSamplingRate;

  /**
   * Drop spans at proxy that completed more than configured minutes ago before reporting.
   */
  private Integer dropSpansDelayedMinutes;

  public boolean getCollectorSetsRetryBackoff() {
    return collectorSetsRetryBackoff;
  }

  public void setCollectorSetsRetryBackoff(boolean collectorSetsRetryBackoff) {
    this.collectorSetsRetryBackoff = collectorSetsRetryBackoff;
  }

  @Nullable
  public Double getRetryBackoffBaseSeconds() {
    return retryBackoffBaseSeconds;
  }

  public void setRetryBackoffBaseSeconds(@Nullable Double retryBackoffBaseSeconds) {
    this.retryBackoffBaseSeconds = retryBackoffBaseSeconds;
  }

  public boolean getCollectorSetsRateLimit() {
    return this.collectorSetsRateLimit;
  }

  public void setCollectorSetsRateLimit(boolean collectorSetsRateLimit) {
    this.collectorSetsRateLimit = collectorSetsRateLimit;
  }

  @Nullable
  public Long getCollectorRateLimit() {
    return this.collectorRateLimit;
  }

  public void setCollectorRateLimit(@Nullable Long collectorRateLimit) {
    this.collectorRateLimit = collectorRateLimit;
  }

  @Nullable
  public Long getHistogramRateLimit() {
    return histogramRateLimit;
  }

  public void setHistogramRateLimit(@Nullable Long histogramRateLimit) {
    this.histogramRateLimit = histogramRateLimit;
  }

  @Nullable
  public Double getSourceTagsRateLimit() {
    return sourceTagsRateLimit;
  }

  public void setSourceTagsRateLimit(@Nullable Double sourceTagsRateLimit) {
    this.sourceTagsRateLimit = sourceTagsRateLimit;
  }

  @Nullable
  public Long getSpanRateLimit() {
    return spanRateLimit;
  }

  public void setSpanRateLimit(@Nullable Long spanRateLimit) {
    this.spanRateLimit = spanRateLimit;
  }

  @Nullable
  public Long getSpanLogsRateLimit() {
    return spanLogsRateLimit;
  }

  public void setSpanLogsRateLimit(@Nullable Long spanLogsRateLimit) {
    this.spanLogsRateLimit = spanLogsRateLimit;
  }

  @Nullable
  public Double getEventsRateLimit() {
    return eventsRateLimit;
  }

  public void setEventsRateLimit(@Nullable Double eventsRateLimit) {
    this.eventsRateLimit = eventsRateLimit;
  }

  @Nullable
  public Long getLogsRateLimit() {
    return logsRateLimit;
  }

  public void setLogsRateLimit(@Nullable Long logsRateLimit) {
    this.logsRateLimit = logsRateLimit;
  }

  public void setCollectorSetsPointsPerBatch(boolean collectorSetsPointsPerBatch) {
    this.collectorSetsPointsPerBatch = collectorSetsPointsPerBatch;
  }

  public boolean getCollectorSetsPointsPerBatch() {
    return collectorSetsPointsPerBatch;
  }

  @Nullable
  public Long getPointsPerBatch() {
    return pointsPerBatch;
  }

  public void setPointsPerBatch(long pointsPerBatch) {
    this.pointsPerBatch = pointsPerBatch;
  }

  public boolean getShutOffAgents() { return shutOffAgents; }

  public void setShutOffAgents(boolean shutOffAgents) {
    this.shutOffAgents = shutOffAgents;
  }

  public boolean getShowTrialExpired() { return showTrialExpired; }

  public void setShowTrialExpired(boolean trialExpired) {
    this.showTrialExpired = trialExpired;
  }

  public boolean getHistogramDisabled() {
    return histogramDisabled;
  }

  public String getShutOffMessage() {
    return shutOffMessage;
  }

  public void setShutOffMessage(String shutOffMessage) {
    this.shutOffMessage = shutOffMessage;
  }

  public void setHistogramDisabled(boolean histogramDisabled) {
    this.histogramDisabled = histogramDisabled;
  }

  public boolean getTraceDisabled() {
    return this.traceDisabled;
  }

  public void setTraceDisabled(boolean traceDisabled) {
    this.traceDisabled = traceDisabled;
  }

  public boolean getSpanLogsDisabled() {
    return spanLogsDisabled;
  }

  public void setSpanLogsDisabled(boolean spanLogsDisabled) {
    this.spanLogsDisabled = spanLogsDisabled;
  }

  public boolean getLogsDisabled() {
    return logsDisabled;
  }

  public void setLogsDisabled(boolean logsDisabled) {
    this.logsDisabled = logsDisabled;
  }

  public String getLogServerEndpointUrl() {
    return logServerEndpointUrl;
  }

  public void setLogServerEndpointUrl(String logServerEndpointUrl) { this.logServerEndpointUrl = logServerEndpointUrl; }

  public String getLogServerToken() {
    return logServerToken;
  }

  public void setLogServerToken(String logServerToken) { this.logServerToken = logServerToken; }

  public boolean isEnableHyperlogsConvergedCsp() {
    return enableHyperlogsConvergedCsp;
  }

  public void setEnableHyperlogsConvergedCsp(boolean enableHyperlogsConvergedCsp) {
    this.enableHyperlogsConvergedCsp = enableHyperlogsConvergedCsp;
  }
  public ValidationConfiguration getValidationConfiguration() {
    return this.validationConfiguration;
  }

  public void setValidationConfiguration(ValidationConfiguration value) {
    this.validationConfiguration = value;
  }

  @Nullable
  public Long getGlobalCollectorRateLimit() {
    return globalCollectorRateLimit;
  }

  public void setGlobalCollectorRateLimit(@Nullable Long globalCollectorRateLimit) {
    this.globalCollectorRateLimit = globalCollectorRateLimit;
  }

  @Nullable
  public Long getGlobalHistogramRateLimit() {
    return globalHistogramRateLimit;
  }

  public void setGlobalHistogramRateLimit(@Nullable Long globalHistogramRateLimit) {
    this.globalHistogramRateLimit = globalHistogramRateLimit;
  }

  @Nullable
  public Double getGlobalSourceTagRateLimit() {
    return globalSourceTagRateLimit;
  }

  public void setGlobalSourceTagRateLimit(@Nullable Double globalSourceTagRateLimit) {
    this.globalSourceTagRateLimit = globalSourceTagRateLimit;
  }

  @Nullable
  public Long getGlobalSpanRateLimit() {
    return globalSpanRateLimit;
  }

  public void setGlobalSpanRateLimit(@Nullable Long globalSpanRateLimit) {
    this.globalSpanRateLimit = globalSpanRateLimit;
  }

  @Nullable
  public Long getGlobalSpanLogsRateLimit() {
    return globalSpanLogsRateLimit;
  }

  public void setGlobalSpanLogsRateLimit(@Nullable Long globalSpanLogsRateLimit) {
    this.globalSpanLogsRateLimit = globalSpanLogsRateLimit;
  }

  @Nullable
  public Double getGlobalEventRateLimit() {
    return globalEventRateLimit;
  }

  public void setGlobalEventRateLimit(@Nullable Double globalEventRateLimit) {
    this.globalEventRateLimit = globalEventRateLimit;
  }

  @Nullable
  public Long getGlobalLogsRateLimit() {
    return globalLogsRateLimit;
  }

  public void setGlobalLogsRateLimit(@Nullable Long globalLogsRateLimit) {
    this.globalLogsRateLimit = globalLogsRateLimit;
  }

  @Nullable
  public Integer getHistogramStorageAccuracy() {
    return histogramStorageAccuracy;
  }

  public void setHistogramStorageAccuracy(@Nullable Integer histogramStorageAccuracy) {
    this.histogramStorageAccuracy = histogramStorageAccuracy;
  }

  @Nullable
  public String getPreprocessorRules() {
    return preprocessorRules;
  }

  public void setPreprocessorRules(@Nullable String preprocessorRules) {
    this.preprocessorRules = preprocessorRules;
  }

  @Nullable
  public List<String> getSystemMetrics() {
    return systemMetrics;
  }

  public void setSystemMetrics(@Nullable List<String> systemMetrics) {
    this.systemMetrics = systemMetrics;
  }

  @Nullable
  public Double getSpanSamplingRate() {
    return spanSamplingRate;
  }

  public void setSpanSamplingRate(@Nullable Double spanSamplingRate) {
    this.spanSamplingRate = spanSamplingRate;
  }

  @Nullable
  public Integer getDropSpansDelayedMinutes() {
    return dropSpansDelayedMinutes;
  }

  public void setDropSpansDelayedMinutes(int dropSpansDelayedMinutes) {
    this.dropSpansDelayedMinutes = dropSpansDelayedMinutes;
  }

  @Nullable
  public List<SpanSamplingPolicy> getActiveSpanSamplingPolicies() {
    return activeSpanSamplingPolicies;
  }

  public void setActiveSpanSamplingPolicies(
      @Nullable List<SpanSamplingPolicy> activeSpanSamplingPolicies) {
    this.activeSpanSamplingPolicies = activeSpanSamplingPolicies;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AgentConfiguration that = (AgentConfiguration) o;

    if (allowAnyHostKeys != that.allowAnyHostKeys) return false;
    if (defaultPublicKey != null ? !defaultPublicKey.equals(that.defaultPublicKey) : that.defaultPublicKey != null)
      return false;
    if (defaultUsername != null ? !defaultUsername.equals(that.defaultUsername) : that.defaultUsername != null)
      return false;
    if (name != null ? !name.equals(that.name) : that.name != null) return false;
    return true;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (defaultUsername != null ? defaultUsername.hashCode() : 0);
    result = 31 * result + (defaultPublicKey != null ? defaultPublicKey.hashCode() : 0);
    result = 31 * result + (allowAnyHostKeys ? 1 : 0);
    return result;
  }

  public boolean isTruncateQueue() {
    return truncateQueue;
  }

  public void setTruncateQueue(boolean truncateQueue) {
    this.truncateQueue = truncateQueue;
  }
}
