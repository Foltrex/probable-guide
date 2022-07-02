package com.scn.jira.common.json;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = FormattedDurationConstraintValidation.class)
@Target({FIELD})
@Retention(RUNTIME)
public @interface FormattedDuration {
    String message() default "Invalid duration";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
