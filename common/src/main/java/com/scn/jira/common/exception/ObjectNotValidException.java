package com.scn.jira.common.exception;

import javax.annotation.Nonnull;
import javax.validation.ConstraintViolationException;

public class ObjectNotValidException extends InternalRuntimeException {

    public ObjectNotValidException(@Nonnull ConstraintViolationException exception) {
        super(exception.getMessage());
        exception.getConstraintViolations()
            .forEach(violation -> errorResult.getErrors().putIfAbsent(violation.getPropertyPath().toString(), violation.getMessage()));
    }
}
