package com.scn.jira.common.ao.tx;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.jira.transaction.Transaction;
import com.atlassian.jira.transaction.Txn;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotationUtils;

import javax.annotation.Nonnull;

class TransactionalInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(@Nonnull MethodInvocation methodInvocation) throws Throwable {
        if (AnnotationUtils.getAnnotation(methodInvocation.getMethod(), Transactional.class) == null
            && AnnotationUtils.getAnnotation(methodInvocation.getClass(), Transactional.class) == null) {
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
