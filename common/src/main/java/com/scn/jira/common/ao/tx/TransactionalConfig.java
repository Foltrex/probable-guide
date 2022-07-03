package com.scn.jira.common.ao.tx;

import org.springframework.context.annotation.Bean;

import javax.annotation.Nonnull;

public class TransactionalConfig {

    @Bean
    @Nonnull
    public static TransactionalBeanPostProcessor transactionalBeanPostProcessor() {
        return new TransactionalBeanPostProcessor();
    }
}
