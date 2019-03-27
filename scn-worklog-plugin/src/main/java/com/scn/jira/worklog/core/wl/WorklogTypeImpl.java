package com.scn.jira.worklog.core.wl;

import com.atlassian.jira.issue.IssueConstantImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.jira.util.BaseUrl;

import org.ofbiz.core.entity.GenericValue;

public class WorklogTypeImpl extends IssueConstantImpl  implements WorklogType
{
  public WorklogTypeImpl(GenericValue genericValue, TranslationManager translationManager, JiraAuthenticationContext authenticationContext, BaseUrl baseUrl)
  {
    super(genericValue, translationManager, authenticationContext, baseUrl);
  }

  public String getStatusColor() {      
    return this.genericValue.getString("statusColor");
  }
}