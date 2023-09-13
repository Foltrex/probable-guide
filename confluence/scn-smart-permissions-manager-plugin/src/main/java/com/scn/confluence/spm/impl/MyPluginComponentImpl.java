package com.scn.confluence.spm.impl;

import com.atlassian.sal.api.ApplicationProperties;
import com.scn.confluence.spm.api.MyPluginComponent;


public class MyPluginComponentImpl implements MyPluginComponent {
    private final ApplicationProperties applicationProperties;
        public MyPluginComponentImpl(final ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public String getName() {
        if(applicationProperties != null) {
            return "myComponent:" + applicationProperties.getDisplayName();
        }
        
        return "myComponent";
    }
}
