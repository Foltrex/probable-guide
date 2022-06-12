package com.scn.jira.automation.impl.rest.mapper;

import com.scn.jira.common.exception.ErrorResult;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class CommonExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(@Nonnull Throwable e) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(new ErrorResult(e.getLocalizedMessage())).build();
    }
}
