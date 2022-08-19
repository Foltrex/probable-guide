package com.scn.jira.common.ao.tx;

import com.atlassian.activeobjects.tx.Transactional;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

class TransactionalBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(@Nonnull Object bean, @Nonnull String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        for (Method method : targetClass.getMethods()) {
            if (AnnotationUtils.getAnnotation(targetClass, Transactional.class) != null
                || AnnotationUtils.getAnnotation(method, Transactional.class) != null) {
                if (bean instanceof Advised) {
                    Advised advised = (Advised) bean;
                    advised.addAdvice(new TransactionalInterceptor());
                    return bean;
                }
                ProxyFactory factory = new ProxyFactory(bean);
                factory.setProxyTargetClass(true);
                factory.addAdvice(new TransactionalInterceptor());
                return factory.getProxy();
            }
        }
        return bean;
    }
}
