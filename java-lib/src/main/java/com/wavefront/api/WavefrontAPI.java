package com.wavefront.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.wavefront.api.agent.AgentConfiguration;
import com.wavefront.api.agent.ShellOutputDTO;

import java.util.UUID;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * API for the Agent.
 *
 * @author Clement Pang (clement@wavefront.com)
 */
@Path("/")
public interface WavefrontAPI {

  /**
   * Polls for the configuration for the agent.
   *
   * @param agentId       Agent id to poll for configuration.
   * @param hostname      Hostname of the agent.
   * @param currentMillis Current millis on the agent (to adjust for timing).
   * @param token         Token to auto-register the agent.
   * @param version       Version of the agent.
   * @return Configuration for the agent.
   */
  @GET
  @Path("daemon/{agentId}/config")
  @Produces(MediaType.APPLICATION_JSON)
  AgentConfiguration getConfig(@PathParam("agentId") UUID agentId,
                               @QueryParam("hostname") String hostname,
                               @QueryParam("currentMillis") final Long currentMillis,
                               @QueryParam("bytesLeftForBuffer") Long bytesLeftForbuffer,
                               @QueryParam("bytesPerMinuteForBuffer") Long bytesPerMinuteForBuffer,
                               @QueryParam("currentQueueSize") Long currentQueueSize,
                               @QueryParam("token") String token,
                               @QueryParam("version") String version);

  @POST
  @Path("daemon/{sshDaemonId}/checkin")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  AgentConfiguration checkin(@PathParam("sshDaemonId") final UUID agentId,
                             @QueryParam("hostname") String hostname,
                             @QueryParam("token") String token,
                             @QueryParam("version") String version,
                             @QueryParam("currentMillis") final Long currentMillis,
                             @QueryParam("local") Boolean localAgent,
                             JsonNode agentMetrics,
                             @QueryParam("push") Boolean pushAgent,
                             @QueryParam("ephemeral") Boolean ephemeral);


  /**
   * Post batched data from pushed data (graphitehead, statsd) that was proxied through the collector.
   *
   * @param agentId       Agent Id of the agent reporting the result.
   * @param workUnitId    Work unit that the agent is reporting.
   * @param currentMillis Current millis on the agent (to adjust for timing).
   * @param format        The format of the data
   * @param pushData      The batched push data
   */
  @POST
  @Consumes(MediaType.TEXT_PLAIN)
  @Path("daemon/{agentId}/pushdata/{workUnitId}")
  Response postPushData(@PathParam("agentId") UUID agentId,
                        @PathParam("workUnitId") UUID workUnitId,
                        @Deprecated @QueryParam("currentMillis") Long currentMillis,
                        @QueryParam("format") String format,
                        String pushData);

  /**
   * Reports an error that occured in the agent.
   *
   * @param agentId Agent reporting the error.
   * @param details Details of the error.
   */
  @POST
  @Path("daemon/{agentId}/error")
  void agentError(@PathParam("agentId") UUID agentId,
                  @FormParam("details") String details);

  @POST
  @Path("daemon/{agentId}/config/processed")
  void agentConfigProcessed(@PathParam("agentId") UUID agentId);

  /**
   * Post work unit results from an agent executing a particular work unit on a host machine.
   *
   * @param agentId        Agent Id of the agent reporting the result.
   * @param workUnitId     Work unit that the agent is reporting.
   * @param targetId       The target that's reporting the result.
   * @param shellOutputDTO The output of running the work unit.
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("daemon/{agentId}/workunit/{workUnitId}/{hostId}")
  Response postWorkUnitResult(@PathParam("agentId") UUID agentId,
                              @PathParam("workUnitId") UUID workUnitId,
                              @PathParam("hostId") UUID targetId,
                              @Valid ShellOutputDTO shellOutputDTO);

  /**
   * Reports that a host has failed to connect.
   *
   * @param agentId Agent reporting the error.
   * @param hostId  Host that is experiencing connection issues.
   * @param details Details of the error.
   */
  @POST
  @Path("daemon/{agentId}/host/{hostId}/fail")
  void hostConnectionFailed(@PathParam("agentId") UUID agentId,
                            @PathParam("hostId") UUID hostId,
                            @FormParam("details") String details);

  /**
   * Reports that a connection to a host has been established.
   *
   * @param agentId Agent reporting the event.
   * @param hostId  Host.
   */
  @POST
  @Path("daemon/{agentId}/host/{hostId}/connect")
  void hostConnectionEstablished(@PathParam("agentId") UUID agentId,
                                 @PathParam("hostId") UUID hostId);

  /**
   * Reports that an auth handshake to a host has been completed.
   *
   * @param agentId Agent reporting the event.
   * @param hostId  Host.
   */
  @POST
  @Path("daemon/{agentId}/host/{hostId}/auth")
  void hostAuthenticated(@PathParam("agentId") UUID agentId,
                         @PathParam("hostId") UUID hostId);

}
