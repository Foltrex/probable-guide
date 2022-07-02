package com.scn.jira.common.exception;

import org.apache.commons.collections4.CollectionUtils;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

public class ObjectValidator {
    private final Validator validator;

    public ObjectValidator() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.usingContext().getValidator();
    }

    public void validate(Object object) {
        Set<ConstraintViolation<Object>> validateResult = validator.validate(object);
        if (CollectionUtils.isNotEmpty(validateResult)) {
            throw new ObjectNotValidException(new ConstraintViolationException(validateResult));
        }
    }
}
