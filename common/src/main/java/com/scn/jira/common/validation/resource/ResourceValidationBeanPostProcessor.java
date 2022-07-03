package com.scn.jira.common.validation.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.ws.rs.Path;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@RequiredArgsConstructor
public class ResourceValidationBeanPostProcessor implements BeanPostProcessor {

    private final Validator validator;

    @Override
    public Object postProcessAfterInitialization(@Nonnull Object bean, @Nonnull String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        for (Method method : targetClass.getMethods()) {
            for (Parameter parameter : method.getParameters()) {
                if (AnnotationUtils.getAnnotation(targetClass, Path.class) != null
                    && AnnotationUtils.getAnnotation(parameter, Valid.class) != null) {
                    if (bean instanceof Advised) {
                        Advised advised = (Advised) bean;
                        advised.addAdvice(0, new ResourceValidationInterceptor(validator));
                        return bean;
                    }
                    ProxyFactory factory = new ProxyFactory(bean);
                    factory.setProxyTargetClass(true);
                    factory.addAdvice(0, new ResourceValidationInterceptor(validator));
                    return factory.getProxy();
                }
            }
        }
        return bean;
    }

}
