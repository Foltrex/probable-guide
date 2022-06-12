package com.scn.jira.common.exception;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Response;

public class EntityNotFoundException extends InternalRuntimeException {

    public EntityNotFoundException(@Nonnull Class<?> entityClass, Object id) {
        this(entityClass, "id", id);
    }

    public EntityNotFoundException(@Nonnull Class<?> entityClass, String key, Object id) {
        super(String.format("Not found entity '%s' with %s='%s'", entityClass.getName(), key, id));
    }

    @Override
    protected Response.Status getResponseStatus() {
        return Response.Status.NOT_FOUND;
    }
}
