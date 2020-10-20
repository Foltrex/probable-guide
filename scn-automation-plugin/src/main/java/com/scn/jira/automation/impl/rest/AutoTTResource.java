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
        return Response.ok(autoTTService.getAll().stream()
            .filter(autoTTValidator::canRead)
            .sorted((o1, o2) -> o2.getUpdated().compareTo(o1.getUpdated()))
            .collect(Collectors.toList())).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") Long id) {
        AutoTTDto autoTTDto = autoTTService.get(id);
        if (autoTTValidator.canRead(autoTTDto)) {
            return Response.ok(autoTTDto).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN)
                .entity(new Validator("No read permissions")).build();
        }
    }

    @POST
    public Response create(AutoTTDto autoTTDto) {
        Validator validator = autoTTValidator.validate(autoTTDto);
        if (validator.isValid()) {
            if (autoTTValidator.canCreate(autoTTDto)) {
                return Response.status(Response.Status.CREATED).entity(autoTTService.add(autoTTDto)).build();
            } else {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity(new Validator("No create permissions")).build();
            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity(validator).build();
        }
    }

    @PUT
    public Response update(AutoTTDto autoTTDto) {
        Validator validator = autoTTValidator.validate(autoTTDto);
        if (validator.isValid()) {
            if (autoTTValidator.canUpdate(autoTTDto)) {
                return Response.ok(autoTTService.update(autoTTDto)).build();
            } else {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity(new Validator("No update permissions")).build();
            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity(validator).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        if (autoTTValidator.canDelete(id)) {
            autoTTService.remove(id);
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.FORBIDDEN)
                .entity(new Validator("No delete permissions")).build();
        }
    }
}
