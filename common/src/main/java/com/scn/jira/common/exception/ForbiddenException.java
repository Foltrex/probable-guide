package com.scn.jira.common.exception;

import javax.ws.rs.core.Response;

public class ForbiddenException extends InternalRuntimeException {

    public ForbiddenException(String message) {
        super(message);

    }

    @Override
    protected Response.Status getResponseStatus() {
        return Response.Status.FORBIDDEN;
    }
}
