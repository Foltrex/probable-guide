package com.scn.jira.integration;

import com.atlassian.sal.api.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IntegrationPluginComponentImpl implements IntegrationPluginComponent {
    private final ApplicationProperties applicationProperties;

    @Override
    public String getName() {
        return "Integration Plugin: " + applicationProperties.getDisplayName();
    }

    @Override
    public String getCloneProjectPluginName() {
        return "Clone Project Plugin";
    }
}
