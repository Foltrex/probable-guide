package com.scn.jira.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IntegrationPluginComponentImpl implements IntegrationPluginComponent {

    @Override
    public String getName() {
        return "Integration plugin";
    }

    @Override
    public String getCloneProjectPluginName() {
        return "Clone Project Plugin";
    }
}
