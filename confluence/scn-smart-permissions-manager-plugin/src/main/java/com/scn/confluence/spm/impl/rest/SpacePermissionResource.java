package com.scn.confluence.spm.impl.rest;

import com.scn.confluence.spm.api.domain.service.SpacePermissionService;
import com.scn.confluence.spm.impl.domain.dto.SpacePermissionDto;
import lombok.RequiredArgsConstructor;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Path("space-permission")
@RequiredArgsConstructor
public class SpacePermissionResource {
    private final SpacePermissionService spacePermissionService;
//
    @GET
    @Path("/")
    public Response getSpacePermissions() {
        return Response
            .ok(spacePermissionService.getSpacePermissions())
            .build();
    }


    @GET
    @Path("{key}")
    public Response getSpacePermissionsBySpaceKey(@PathParam("key") @DefaultValue("") String key) {
        List<SpacePermissionDto> list = spacePermissionService.getSpacePermissionBySpaceKey(key);
        return Response
            .ok(spacePermissionService.getSpacePermissionBySpaceKey(key))
            .build();
    }

    @POST
    public Response createSpacePermission(SpacePermissionDto bean) {
        return Response.ok(bean).build();
    }

    @DELETE
    @Path ("{id}")
    public Response delete(@PathParam ("id") @DefaultValue("0") long id) {
        return Response.noContent().build();
    }
}
