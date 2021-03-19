package com.scn.jira.timesheet.report.timesheet;

import java.util.Comparator;

import com.atlassian.jira.issue.Issue;

public class IssueProjectComparator implements Comparator<Issue> {
	@Override
	public int compare(Issue o1, Issue o2) {
		if ((((o1 == null) || (o1 instanceof Issue))) && (((o2 == null) || (o2 instanceof Issue)))) {
			if ((o1 == null) && (o2 == null))
				return 0;
			if (o1 == null)
				return -1;
			if (o2 == null) {
				return 1;
			}
			return o1.getKey().compareTo(o2.getKey());
		}

		throw new IllegalArgumentException("Object passed must be null or of type Issue");
	}
}
