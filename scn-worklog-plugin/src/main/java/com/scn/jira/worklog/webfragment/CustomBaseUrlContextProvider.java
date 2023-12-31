package com.scn.jira.worklog.webfragment;

import com.atlassian.jira.plugin.webfragment.contextproviders.BaseUrlContextProvider;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class CustomBaseUrlContextProvider extends BaseUrlContextProvider {

    @Inject
    public CustomBaseUrlContextProvider(VelocityRequestContextFactory requestContextFactory) {
        super(requestContextFactory);
    }
}
