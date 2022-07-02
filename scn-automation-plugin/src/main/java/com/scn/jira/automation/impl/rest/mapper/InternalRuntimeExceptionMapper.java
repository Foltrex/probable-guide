package com.scn.jira.automation.impl.rest.mapper;

import com.scn.jira.common.exception.InternalRuntimeException;

import javax.annotation.Nonnull;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
@Produces({MediaType.APPLICATION_JSON})
public class InternalRuntimeExceptionMapper implements ExceptionMapper<InternalRuntimeException> {

    @Override
    public Response toResponse(@Nonnull InternalRuntimeException e) {
        return e.getResponse();
    }
}
