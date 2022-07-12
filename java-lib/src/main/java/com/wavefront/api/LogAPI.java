package com.wavefront.api;

import com.wavefront.dto.Log;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/")
public interface LogAPI {

    /**
     * Ingest a batch of logs.
     *
     * @param logBatch batch of logs to be reported
     * @return HTTP response
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("le-mans/v1/streams/ingestion-pipeline-stream")
    Response proxyLogs(@HeaderParam("agent") final String agentProxyId, final List<Log> logBatch);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("le-mans/v1/streams/ingestion-pipeline-stream")
    Response proxyLogsStr(@HeaderParam("agent") final String agentProxyId, final String logBatch);
}
