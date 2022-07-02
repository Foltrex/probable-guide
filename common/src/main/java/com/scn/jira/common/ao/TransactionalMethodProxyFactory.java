package com.scn.jira.common.ao;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.jira.transaction.Transaction;
import com.atlassian.jira.transaction.Txn;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.core.annotation.AnnotationUtils;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

public class TransactionalMethodProxyFactory implements MethodInterceptor {

    public Object createProxyIfRequired(@Nonnull Object bean) throws BeansException {
        for (Method method : bean.getClass().getMethods()) {
            Transactional annotation = AnnotationUtils.getAnnotation(method, Transactional.class);
            if (annotation != null) {
                ProxyFactory factory = new ProxyFactory(bean);
                factory.setProxyTargetClass(true);
                factory.addAdvice(this);
                return factory.getProxy();
            }
        }
        return bean;
    }

    @Override
    public Object invoke(@Nonnull MethodInvocation methodInvocation) throws Throwable {
        Transactional annotation = AnnotationUtils.getAnnotation(methodInvocation.getMethod(), Transactional.class);
        if (annotation == null) {
            return methodInvocation.proceed();
        }
        Transaction txn = Txn.begin();
        try {
            Object result = methodInvocation.proceed();
            txn.commit();
            return result;
        } finally {
            txn.finallyRollbackIfNotCommitted();
        }
    }
}
