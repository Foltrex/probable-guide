package com.scn.confluence.spm.impl.rest;

import com.scn.confluence.spm.api.domain.service.SpacePermissionService;
import com.scn.confluence.spm.impl.domain.dto.SpacePermissionDto;
import lombok.RequiredArgsConstructor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Path("space-permission")
@RequiredArgsConstructor
@Consumes({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class SpacePermissionResource {
    private final SpacePermissionService spacePermissionService;

    @GET
    @Path("all")
    public Response getSpacePermissions() {
        return Response
            .ok(spacePermissionService.getSpacePermissions())
            .build();
    }


    @GET
    @Path("self/{key}")
    public Response getSpacePermissionsBySpaceKey(@PathParam("key") @DefaultValue("") String key) {
        return Response
            .ok(spacePermissionService.getSpacePermissionBySpaceKey(key))
            .build();
    }

    @POST
    @Path("self")
    public Response createSpacePermission(SpacePermissionDto bean) {
        return Response.ok(bean).build();
    }

    @DELETE
    @Path ("self/{id}")
    public Response delete(@PathParam ("id") @DefaultValue("0") long id) {
        return Response.noContent().build();
    }
}
