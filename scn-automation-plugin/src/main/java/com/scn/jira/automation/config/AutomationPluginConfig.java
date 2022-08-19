package com.scn.jira.automation.config;

import com.scn.jira.common.ao.tx.TransactionalConfig;
import com.scn.jira.common.validation.ValidationConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TransactionalConfig.class, ValidationConfig.class})
public class AutomationPluginConfig {
}
