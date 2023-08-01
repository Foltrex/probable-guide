package com.scn.jira.worklog.rest;

import com.scn.jira.worklog.api.domain.service.WLTypeService;
import com.scn.jira.worklog.impl.domain.dto.WLTypeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Path("/worklog-types")
@Provider
@Component
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@RequiredArgsConstructor
public class WLTypeResource {
    private final WLTypeService wlTypeService;

    @GET
    public Response getAll(
        @QueryParam("sort_by") @DefaultValue("sequence") String column,
        @QueryParam("order_by") @DefaultValue("asc") String order
    ) {
        return Response.ok(wlTypeService.getAll(column, order)).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") Long id) {
        return Response.ok(wlTypeService.get(id)).build();
    }

    @POST
    public Response create(@Valid WLTypeDto wlTypeDto) {
        return Response.status(Response.Status.CREATED).entity(wlTypeService.create(wlTypeDto)).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, @Valid WLTypeDto wlTypeDto) {
        return Response.ok(wlTypeService.update(id, wlTypeDto)).build();
    }

    @DELETE
    public Response deleteAll() {
        wlTypeService.deleteAll();
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        wlTypeService.deleteById(id);
        return Response.noContent().build();
    }
}
