package com.scn.jira.automation.impl.rest.mapper;

import com.scn.jira.common.exception.ErrorResult;

import javax.annotation.Nonnull;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
@Produces({MediaType.APPLICATION_JSON})
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(@Nonnull ConstraintViolationException e) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(new ErrorResult(e.getMessage()))
            .build();
    }
}
