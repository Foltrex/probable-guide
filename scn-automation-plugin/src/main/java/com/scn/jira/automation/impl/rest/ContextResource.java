package com.scn.jira.automation.impl.rest;

import com.scn.jira.automation.api.domain.service.WorklogContextService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Path("/")
@Named
@Provider
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public class ContextResource {
    private final WorklogContextService worklogContextService;

    @Autowired
    public ContextResource(WorklogContextService worklogContextService) {
        this.worklogContextService = worklogContextService;
    }

    @GET
    @Path("/worklog/type")
    public Response getAllWorklogTypes() {
        return Response.ok(worklogContextService.getAllWorklogTypes()).build();
    }
}
