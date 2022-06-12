package com.scn.jira.automation.impl.rest;

import com.scn.jira.automation.api.domain.service.AutoTTService;
import com.scn.jira.automation.impl.domain.dto.AutoTTDto;
import lombok.RequiredArgsConstructor;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Path("/autotimetracking/user")
@Provider
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@RequiredArgsConstructor
public class AutoTTResource {
    private final AutoTTService autoTTService;

    @GET
    public Response getAll() {
        return Response.ok(autoTTService.getAll()).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") Long id) {
        return Response.ok(autoTTService.get(id)).build();
    }

    @POST
    public Response create(AutoTTDto autoTTDto) {
        return Response.status(Response.Status.CREATED).entity(autoTTService.add(autoTTDto)).build();
    }

    @PUT
    public Response update(@Valid AutoTTDto autoTTDto) {
        return Response.ok(autoTTService.update(autoTTDto)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        autoTTService.remove(id);
        return Response.noContent().build();
    }
}
