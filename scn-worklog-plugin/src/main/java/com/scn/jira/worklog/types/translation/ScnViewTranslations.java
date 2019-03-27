package com.scn.jira.worklog.types.translation;

import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.WorklogType;
import com.scn.jira.worklog.types.ExtendedViewTranslations;

import java.util.Collection;
import java.util.Iterator;
import org.ofbiz.core.entity.GenericValue;

public class ScnViewTranslations
{
  public static final String ISSUECONSTANT_WORKLOGTYPE = "WorklogType";
  public static final String LINKNAME_WORKLOGTYPE = "worklog types";
  protected ExtendedConstantsManager extendedConstantsManager;
  protected ExtendedTranslationManager extendedTranslationManager;
  protected ExtendedViewTranslations viewTranslations;

  public ScnViewTranslations(
		  ExtendedViewTranslations _viewTranslations, 
		  ExtendedConstantsManager extendedConstantsManager, 
		  ExtendedTranslationManager _extendedTranslationManager)
  {
    this.viewTranslations = _viewTranslations;
    this.extendedConstantsManager = extendedConstantsManager;
    this.extendedTranslationManager = _extendedTranslationManager;
  }

    public String getIssueConstantName(String _issueConstantName) {
    if ((_issueConstantName == null) &&
      ("WorklogType".equals(this.viewTranslations.getIssueConstantType()))) {
      _issueConstantName = this.viewTranslations.getJiraServiceContext().getI18nBean().getText("admin.issue.constant.worklogtype");
    }

    return _issueConstantName;
  }

  public String getIssueConstantTranslationPrefix(String _issueConstantTranslationPrefix)
  {
    if (_issueConstantTranslationPrefix == null) {
      _issueConstantTranslationPrefix = this.extendedTranslationManager.getTranslationPrefix(this.viewTranslations.getIssueConstantType());
    }
    return _issueConstantTranslationPrefix;
  }

  public String getRedirectPage(String _redirectPage) {
    if ((_redirectPage == null) &&
      ("WorklogType".equals(this.viewTranslations.getIssueConstantType()))) {
      _redirectPage = "ViewWorklogTypes.jspa";
    }

    return _redirectPage;
  }

  public Collection getIssueConstants(Collection _issueConstants)
  {
    Iterator iterator;
    if ("WorklogType".equals(this.viewTranslations.getIssueConstantType()))
    {
      Collection<GenericValue> worklogTypes = getExtendedConstantsManager().getWorklogTypes();
      for (iterator = worklogTypes.iterator(); iterator.hasNext(); ) {
        GenericValue worklogTypeGV = (GenericValue)iterator.next();
        WorklogType worklogType = getExtendedConstantsManager().getWorklogTypeObject(worklogTypeGV.getString("id"));
        _issueConstants.add(worklogType);
      }
    }
    return _issueConstants;
  }

  public String getLinkName(String _linkName) {
    if ((_linkName == null) &&
      ("WorklogType".equals(this.viewTranslations.getIssueConstantType()))) {
      _linkName = "worklog types";
    }

    return _linkName;
  }

  public ExtendedConstantsManager getExtendedConstantsManager() {
    return this.extendedConstantsManager;
  }
}