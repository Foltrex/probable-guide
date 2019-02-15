package com.scn.jira.worklog.customfield;

import com.atlassian.jira.issue.customfields.searchers.TextSearcher;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.web.FieldVisibilityManager;

public class CustomTextSearcher extends TextSearcher {

    public CustomTextSearcher(FieldVisibilityManager fieldVisibilityManager, JqlOperandResolver jqlOperandResolver, CustomFieldInputHelper customFieldInputHelper) {
        super(fieldVisibilityManager, jqlOperandResolver, customFieldInputHelper);
    }
}
