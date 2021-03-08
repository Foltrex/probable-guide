package com.scn.jira.cloneproject;

import org.springframework.stereotype.Component;

@Component
public class CloneProjectPluginComponentImpl implements CloneProjectPluginComponent {
    public String getName() {
        return "Clone project plugin";
    }
}
