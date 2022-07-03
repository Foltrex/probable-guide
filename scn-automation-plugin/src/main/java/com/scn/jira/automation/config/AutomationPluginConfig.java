package com.scn.jira.automation.config;

import com.scn.jira.common.ao.tx.TransactionalConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TransactionalConfig.class})
public class AutomationPluginConfig {
}
