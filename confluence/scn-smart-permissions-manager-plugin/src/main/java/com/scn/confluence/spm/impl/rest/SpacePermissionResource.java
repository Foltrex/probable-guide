package com.scn.confluence.spm.impl.rest;

import com.scn.confluence.spm.api.domain.service.SpacePermissionService;
import com.scn.confluence.spm.impl.domain.dto.SpacePermissionDto;
import com.scn.confluence.spm.impl.security.SecurityUtils;
import lombok.RequiredArgsConstructor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("space-permission")
@RequiredArgsConstructor
@Consumes({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class SpacePermissionResource {
    private final SpacePermissionService spacePermissionService;
    private final SecurityUtils securityUtils;

    @GET
    @Path("all")
    public Response getSpacePermissions() {
        return securityUtils.isLoggedInUserSuperAdmin()
            ? Response.ok(spacePermissionService.getSpacePermissions()).build()
            : Response.status(Response.Status.FORBIDDEN).build();
    }

    @GET
    @Path("all/{key}")
    public Response getSpacePermissionsBySpaceKey(@PathParam("key") @DefaultValue("") String key) {
        return securityUtils.isLoggedInUserSuperAdmin()
            ? Response.ok(spacePermissionService.getSpacePermissionBySpaceKey(key)).build()
            : Response.status(Response.Status.FORBIDDEN).build();
    }

    @POST
    @Path("self")
    public Response createSpacePermission(SpacePermissionDto bean) {
        return securityUtils.isLoggedInUserSuperAdmin()
            ? Response.ok(spacePermissionService.createSpacePermission(bean)).build()
            : Response.status(Response.Status.FORBIDDEN).build();
    }

    @PUT
    @Path("self/{id}")
    public Response updateSpacePermission(@PathParam("id") String id, SpacePermissionDto bean) {
        String[] compoundKey = id.split("-");
        String spaceKey = compoundKey[0];
        String username = compoundKey[1];
        return securityUtils.isLoggedInUserSuperAdmin()
            ? Response.ok(spacePermissionService.updateSpacePermission(spaceKey, username, bean)).build()
            : Response.status(Response.Status.FORBIDDEN).build();
    }

    @DELETE
    @Path ("self/{id}")
    public Response delete(@PathParam ("id") @DefaultValue("0") String id) {
        String[] compoundKey = id.split("-");
        String spaceKey = compoundKey[0];
        String username = compoundKey[1];
        spacePermissionService.deleteSpacePermission(spaceKey, username);
        return securityUtils.isLoggedInUserSuperAdmin()
            ? Response.noContent().build()
            : Response.status(Response.Status.FORBIDDEN).build();
    }
}
