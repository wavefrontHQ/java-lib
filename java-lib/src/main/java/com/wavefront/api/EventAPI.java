package com.wavefront.api;

import com.wavefront.dto.EventDTO;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/")
public interface EventAPI {

  /**
   * Ingests and persists a batch of events.
   *
   * @param eventBatch batch of events to be reported
   * @return HTTP response
   */
  @Consumes(MediaType.APPLICATION_JSON)
  @POST
  @Path("v2/wfproxy/event")
  Response reportEvents(List<EventDTO> eventBatch);
}
