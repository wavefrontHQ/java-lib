package com.wavefront.ingester;

public class TooManyCentroidException extends RuntimeException {

  public TooManyCentroidException() {
    super();
  }

  public TooManyCentroidException(String message) {
    super(message);
  }
}
