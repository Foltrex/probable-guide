package com.scn.jira.common.json;

import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.JiraDurationUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Locale;

public class FormattedDurationConstraintValidation implements ConstraintValidator<FormattedDuration, String> {
    private JiraDurationUtils jiraDurationUtils;

    @Override
    public void initialize(FormattedDuration constraintAnnotation) {
        jiraDurationUtils = ComponentAccessor.getComponent(JiraDurationUtils.class);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            jiraDurationUtils.parseDuration(value, Locale.ENGLISH);
        } catch (InvalidDurationException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getLocalizedMessage()).addConstraintViolation();
            return false;
        }
        return true;
    }
}
