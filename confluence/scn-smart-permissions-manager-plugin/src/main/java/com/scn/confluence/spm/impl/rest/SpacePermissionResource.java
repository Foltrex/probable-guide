package com.scn.confluence.spm.impl.rest;

import com.scn.confluence.spm.api.domain.service.SpacePermissionService;
import lombok.RequiredArgsConstructor;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("space-permission")
@RequiredArgsConstructor
public class SpacePermissionResource {
    private final SpacePermissionService spacePermissionService;

    @GET
    @Path("/")
    public Response getSpacePermissions() {
        return Response
            .ok(spacePermissionService.getSpacePermissions())
            .build();
    }

//
//    @GET
//    @Path("{key}")
//    public Response getSpacePermissionsBySpaceKey(@PathParam("key") @DefaultValue("") String key) {
//        return Response
//            .ok(spacePermissionService.getSpacePermissionBySpaceKey(key))
//            .build();
//    }
//
//    @PUT
//    @Path ("{id}")
//    public Response updateSpacePermission(@PathParam ("id") final String id, SpacePermissionDto bean) {
//        return null;
//    }
//
//
//    @POST
//    public Response createSpacePermission(SpacePermissionDto bean) {
//        return null;
//    }
//
//    @DELETE
//    @Path ("{id}")
//    public Response delete(@PathParam ("id") String id) {
//        return null;
//    }
}
