package com.scn.jira.common.validation.resource;

import lombok.RequiredArgsConstructor;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.annotation.AnnotationUtils;

import javax.annotation.Nonnull;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validator;
import java.util.Set;

@RequiredArgsConstructor
public class ResourceValidationInterceptor implements MethodInterceptor {

    private final Validator validator;

    @Override
    public Object invoke(@Nonnull MethodInvocation methodInvocation) throws Throwable {
        for (int i = 0; i < methodInvocation.getMethod().getParameters().length; i++) {
            if (AnnotationUtils.getAnnotation(methodInvocation.getMethod().getParameters()[i], Valid.class) != null) {
                Set<ConstraintViolation<Object>> validateResult = validator.validate(methodInvocation.getArguments()[i]);
                if (CollectionUtils.isNotEmpty(validateResult)) {
                    throw new ConstraintViolationException(validateResult);
                }
            }
        }
        return methodInvocation.proceed();
    }
}
