package com.scn.jira.worklog.rest;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;

import javax.annotation.Nonnull;
import javax.ws.rs.core.CacheControl;

abstract class BaseResource {
    private final JiraAuthenticationContext jiraAuthenticationContext;

    protected BaseResource(JiraAuthenticationContext jiraAuthenticationContext) {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    protected ApplicationUser getApplicationUser() {
        return jiraAuthenticationContext.getLoggedInUser();
    }

    @Nonnull
    protected CacheControl getNoCacheControl() {
        CacheControl noCache = new CacheControl();
        noCache.setNoCache(true);
        return noCache;
    }
}
