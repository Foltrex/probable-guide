package com.scn.jira.worklog.core.wl;

import com.atlassian.jira.issue.IssueConstant;

import java.util.Collection;

import org.ofbiz.core.entity.GenericValue;

public interface ExtendedConstantsManager {
    String WORKLOGTYPE_CONSTANT_TYPE = "WorklogType";

    WorklogType getWorklogTypeObject(String paramString);

    GenericValue getWorklogType(String paramString);

    Collection<GenericValue> getWorklogTypes();

    Collection<WorklogType> getWorklogTypeObjects();

    void refreshWorklogTypes();

    IssueConstant getIssueConstant(GenericValue paramGenericValue);

    GenericValue getConstant(String paramString1, String paramString2);

    IssueConstant getConstantObject(String paramString1, String paramString2);

    GenericValue getConstantByName(String paramString1, String paramString2);
}
