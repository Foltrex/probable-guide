package com.scn.jira.worklog.workflow;

import java.util.Map;

import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.google.common.collect.Lists;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.WorkflowException;

public class ZeroedRemainingTimePostFunction implements FunctionProvider
{
	//private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ZeroedRemainingTimePostFunction.class);
	
	private static final Long ZERO = 0L;
	
	public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException
	{
		final MutableIssue issue = (MutableIssue) transientVars.get("issue");
		final Long oldEstimate = issue.getEstimate();
			
		if (!ZERO.equals(oldEstimate))
		{
			issue.setEstimate(ZERO);
			
			String from = ZERO.toString();
			String to = (oldEstimate == null) ? null : oldEstimate.toString();
			transientVars.put("changeItems", Lists.newArrayList(new ChangeItemBean("jira", "timeestimate", from, from, to, to)));
		}
	}
}
