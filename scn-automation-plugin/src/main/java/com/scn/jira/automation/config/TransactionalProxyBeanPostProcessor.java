package com.scn.jira.automation.config;

import com.scn.jira.common.ao.TransactionalMethodProxyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

@Configuration
@RequiredArgsConstructor
public class TransactionalProxyBeanPostProcessor implements BeanPostProcessor {
    private final TransactionalMethodProxyFactory transactionalMethodProxyFactory;

    @Override
    public Object postProcessAfterInitialization(@Nonnull Object bean, @Nonnull String beanName) throws BeansException {
        return transactionalMethodProxyFactory.createProxyIfRequired(bean);
    }
}
