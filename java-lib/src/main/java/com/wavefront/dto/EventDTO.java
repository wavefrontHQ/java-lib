package com.wavefront.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import wavefront.report.Event;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Wrapper for the Event class.
 *
 * @author vasily@wavefront.com
 */
public class EventDTO implements Serializable {
  private Event event;

  public EventDTO(Event event) {
    this.event = event;
  }

  @JsonProperty
  public String getName() {
    return event.getName();
  }

  @JsonProperty
  public long getStartTime() {
    return event.getStartTime();
  }

  @JsonProperty
  public Long getEndTime() {
    return event.getEndTime();
  }

  @JsonProperty
  public Map<String, String> getAnnotations() {
    return event.getAnnotations();
  }

  @JsonProperty
  public List<String> getHosts() {
    return event.getHosts();
  }

  @JsonProperty
  public List<String> getTags() {
    return event.getTags();
  }
}
