package com.scn.jira.logtime;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.ApplicationProperties;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class MyPluginComponentImpl implements MyPluginComponent
{
    private final ApplicationProperties applicationProperties;

    @Inject
    public MyPluginComponentImpl(@ComponentImport ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    public String getName()
    {
        if(null != applicationProperties)
        {
            return "myComponent:" + "NAME!";//applicationProperties.getDisplayName();
        }
        
        return "myComponent";
    }
}