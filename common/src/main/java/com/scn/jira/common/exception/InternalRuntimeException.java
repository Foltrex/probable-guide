package com.scn.jira.common.exception;

import javax.ws.rs.core.Response;

public class InternalRuntimeException extends RuntimeException {
    protected final ErrorResult errorResult;

    public InternalRuntimeException(String message, Throwable cause) {
        super(message, cause);
        errorResult = new ErrorResult(message);
    }

    public InternalRuntimeException(String message) {
        super(message);
        errorResult = new ErrorResult(message);
    }

    protected InternalRuntimeException() {
        super();
        errorResult = new ErrorResult();
    }

    public Response getResponse() {
        return Response.status(getResponseStatus()).entity(errorResult).build();
    }

    protected Response.Status getResponseStatus() {
        return Response.Status.BAD_REQUEST;
    }
}
