package com.scn.jira.worklog.webfragment;

import com.atlassian.jira.plugin.webfragment.contextproviders.XsrfTokenContextProvider;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class CustomXsrfTokenContextProvider extends XsrfTokenContextProvider {

    @Inject
    public CustomXsrfTokenContextProvider(XsrfTokenGenerator xsrfTokenGenerator) {
        super(xsrfTokenGenerator);
    }
}
