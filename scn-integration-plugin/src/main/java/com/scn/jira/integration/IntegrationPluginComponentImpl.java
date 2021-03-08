package com.scn.jira.integration;

import com.scn.jira.cloneproject.CloneProjectPluginComponent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IntegrationPluginComponentImpl implements IntegrationPluginComponent {
    private final CloneProjectPluginComponent cloneProjectPluginComponent;

    @Override
    public String getName() {
        return "Integration plugin";
    }

    @Override
    public String getCloneProjectPluginName() {
        return "Clone Project Plugin";
    }
}
