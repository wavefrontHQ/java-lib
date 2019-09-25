package com.wavefront.ingester;

import org.antlr.v4.runtime.Token;
import wavefront.report.Annotation;
import wavefront.report.Event;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

import static com.wavefront.ingester.EventDecoder.ONGOING_EVENT;

/**
 * Ingestion formatter for events.
 *
 * @author vasily@wavefront.com.
 */
public class EventIngesterFormatter extends AbstractIngesterFormatter<Event> {

  private EventIngesterFormatter(List<FormatterElement> elements) {
    super(elements);
  }

  /**
   * A builder pattern to create a format for the report point parse.
   */
  public static class EventIngesterFormatBuilder extends IngesterFormatBuilder<Event> {

    @Override
    public EventIngesterFormatter build() {
      return new EventIngesterFormatter(elements);
    }
  }

  public static IngesterFormatBuilder<Event> newBuilder() {
    return new EventIngesterFormatBuilder();
  }

  @Override
  public Event drive(String input, String defaultHostName, String customerId,
                     @Nullable List<String> customSourceTags) {
    Queue<Token> queue = getQueue(input);

    final Event event = new Event();
    event.setAnnotations(new HashMap<>());
    EventWrapper wrapper = new EventWrapper(event);
    try {
      for (FormatterElement element : elements) {
        element.consume(queue, wrapper);
      }
    } catch (Exception ex) {
      throw new RuntimeException("Could not parse: " + input, ex);
    }

    for (Annotation annotation : wrapper.getAnnotationList()) {
      switch (annotation.getKey()) {
        case "host":
          if (event.getHosts() == null) {
            event.setHosts(new ArrayList<>());
          }
          event.getHosts().add(annotation.getValue());
          break;
        case "tag":
        case "eventTag":
          if (event.getTags() == null) {
            event.setTags(new ArrayList<>());
          }
          event.getTags().add(annotation.getValue());
          break;
        default:
          event.getAnnotations().put(annotation.getKey(), annotation.getValue());
      }
    }
    // if no end time specified but it's not an ongoing event, we assume it's an instant event
    if (event.getEndTime() == null && !wrapper.getLiteral().equals(ONGOING_EVENT)) {
      event.setEndTime(event.getStartTime() + 1);
    }
    return Event.newBuilder(event).build();
  }
}
