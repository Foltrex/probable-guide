package com.scn.jira.logtime.resource;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import javax.ws.rs.core.CacheControl;

public class BaseResource {
    protected JiraAuthenticationContext authenticationContext;

    @Autowired
    public void setAuthenticationContext(JiraAuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    protected ApplicationUser getLoggedInUser() {
        return authenticationContext.getLoggedInUser();
    }

    @Nonnull
    protected CacheControl getNoCacheControl() {
        CacheControl noCache = new CacheControl();
        noCache.setNoCache(true);
        return noCache;
    }

    protected String getWlIdFromRequestParameter(String identifier, int i) {
        // TESS-1_10000_0_08-01_143
        // 2-wlid
        // 3-date
        // 1-wlTypeId
        // 0-issueId
        if (identifier != null && identifier.contains("_")) {
            String[] arr = identifier.split("_");
            return arr[i];
        } else {
            return "";
        }
    }

    protected String changeWlIdFromRequestParameter(String identifier, Long newWLId) {
        // TESS-1_10000_0_08-01_143
        // 2-wlid
        // 3-date
        // 1-wlTypeId
        // 0-issueId
        String res = identifier;
        if (identifier != null && identifier.contains("_")) {
            String[] arr = identifier.split("_");
            res = arr[0] + "_" + arr[1] + "_" + newWLId + "_" + arr[3] + "_" + arr[4] + "_" + arr[5];
        }

        return res;
    }
}
