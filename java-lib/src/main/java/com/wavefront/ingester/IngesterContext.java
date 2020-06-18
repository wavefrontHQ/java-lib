package com.wavefront.ingester;

public class IngesterContext {
  public static final int DEFAULT_CENTROIDS_COUNT_LIMIT = 100;
  public static final int DEFAULT_HISTOGRAM_ACCURACY = 32;

  private int histogramCentroidsLimit;
  private int targetHistogramAccuracy;

  public static class Builder {
    private int histogramCentroidsLimit = DEFAULT_CENTROIDS_COUNT_LIMIT;
    private int targetHistogramAccuracy = DEFAULT_HISTOGRAM_ACCURACY;

    public Builder withTargetHistogramAccuracy(int targetHistogramAccuracy) {
      this.targetHistogramAccuracy = targetHistogramAccuracy;
      return this;
    }

    public Builder throwIfTooManyHistogramCentroids(int histogramCentroidsLimit) {
      this.histogramCentroidsLimit = histogramCentroidsLimit;
      return this;
    }

    public IngesterContext build() {
      return new IngesterContext(this);
    }

  }

  private IngesterContext(Builder builder) {
    this.histogramCentroidsLimit = builder.histogramCentroidsLimit;
    this.targetHistogramAccuracy = builder.targetHistogramAccuracy;
  }

  public void reset() {
    this.histogramCentroidsLimit = DEFAULT_CENTROIDS_COUNT_LIMIT;
    this.targetHistogramAccuracy = DEFAULT_HISTOGRAM_ACCURACY;
  }

  public int getHistogramCentroidsLimit() {
    return histogramCentroidsLimit;
  }

  public void setHistogramCentroidsLimit(int histogramCentroidsLimit) {
    this.histogramCentroidsLimit = histogramCentroidsLimit;
  }

  public int getTargetHistogramAccuracy() {
    return targetHistogramAccuracy;
  }

  public void setTargetHistogramAccuracy(int targetHistogramAccuracy) {
    this.targetHistogramAccuracy = targetHistogramAccuracy;
  }
}
