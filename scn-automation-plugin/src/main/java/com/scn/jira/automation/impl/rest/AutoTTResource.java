package com.scn.jira.automation.impl.rest;

import com.scn.jira.automation.api.domain.service.AutoTTService;
import com.scn.jira.automation.api.domain.validator.AutoTTValidator;
import com.scn.jira.automation.impl.domain.dto.AutoTTDto;
import com.scn.jira.automation.impl.domain.dto.Validator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.stream.Collectors;

@Path("/autotimetracking/user")
@Named
@Provider
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public class AutoTTResource {
    private final AutoTTService autoTTService;
    private final AutoTTValidator autoTTValidator;

    @Autowired
    public AutoTTResource(AutoTTService autoTTService,
                          AutoTTValidator autoTTValidator) {
        this.autoTTService = autoTTService;
        this.autoTTValidator = autoTTValidator;
    }

    @GET
    public Response getAll() {
        if (autoTTValidator.canView()) {
            return Response.ok(autoTTService.getAll().stream()
                .sorted((o1, o2) -> o2.getUpdated().compareTo(o1.getUpdated()))
                .collect(Collectors.toList())).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity("No view permissions.").build();
        }
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") Long id) {
        if (autoTTValidator.canView()) {
            return Response.ok(autoTTService.get(id)).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity("No view permissions.").build();
        }
    }

    @POST
    public Response create(AutoTTDto autoTTDto) {
        if (autoTTValidator.canCreate(autoTTDto)) {
            Validator validator = autoTTValidator.validate(autoTTDto);
            if (validator.isValid()) {
                return Response.status(Response.Status.CREATED).entity(autoTTService.add(autoTTDto)).build();
            } else {
                return Response.status(Response.Status.PRECONDITION_FAILED).entity(validator.getErrorMessages()).build();
            }
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity("No create permissions.").build();
        }
    }

    @PUT
    public Response update(AutoTTDto autoTTDto) {
        if (autoTTValidator.canUpdate(autoTTDto)) {
            Validator validator = autoTTValidator.validate(autoTTDto);
            if (validator.isValid()) {
                return Response.ok(autoTTService.update(autoTTDto)).build();
            } else {
                return Response.status(Response.Status.PRECONDITION_FAILED).entity(validator.getErrorMessages()).build();
            }
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity("No update permissions.").build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        if (autoTTValidator.canDelete(id)) {
            autoTTService.remove(id);
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity("No delete permissions").build();
        }
    }
}
