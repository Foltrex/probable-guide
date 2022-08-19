package com.scn.jira.common.validation;

import com.scn.jira.common.validation.resource.ResourceValidationBeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import javax.annotation.Nonnull;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class ValidationConfig {

    @Bean
    public Validator validator() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        return validatorFactory.getValidator();
    }

    @Bean
    @Nonnull
    public static ResourceValidationBeanPostProcessor resourceValidationBeanPostProcessor(Validator validator) {
        return new ResourceValidationBeanPostProcessor(validator);
    }

    @Bean
    @Nonnull
    public static MethodValidationPostProcessor methodValidationPostProcessor(Validator validator) {
        MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        processor.setProxyTargetClass(true);
        processor.setValidator(validator);
        processor.setBeforeExistingAdvisors(true);
        return processor;
    }
}
