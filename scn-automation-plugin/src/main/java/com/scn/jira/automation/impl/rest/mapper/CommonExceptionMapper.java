package com.scn.jira.automation.impl.rest.mapper;

import com.scn.jira.common.exception.ErrorResult;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
@Produces({MediaType.APPLICATION_JSON})
public class CommonExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(@Nonnull Exception e) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(new ErrorResult(StringUtils.isNotBlank(e.getLocalizedMessage()) ? e.getLocalizedMessage() : e.getClass().toString()))
            .build();
    }
}
