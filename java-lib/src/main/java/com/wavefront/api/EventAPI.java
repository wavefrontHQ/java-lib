package com.wavefront.api;

import com.wavefront.dto.EventDTO;
import wavefront.report.Event;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public interface EventAPI {

  @Consumes(MediaType.APPLICATION_JSON)
  @POST
  @Path("v2/event")
  Response createEvent(EventDTO event);
}
