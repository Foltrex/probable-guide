package com.scn.jira.timesheet.report.timesheet;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comparator.IssueKeyComparator;
import com.atlassian.jira.issue.comparator.UserComparator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.scn.jira.timesheet.util.WeekPortletHeader;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Data
public class TimeSheetDto {
    private final List<WeekPortletHeader> weekDays = new ArrayList<>();
    private final Map<Issue, List<IScnWorklog>> allWorkLogs = new Hashtable<>();
    private final Map<ApplicationUser, Map<Issue, Map<IScnWorklog, Long>>> weekWorkLog = new TreeMap<>(new UserComparator());
    private final Map<Issue, Map<Date, Long>> weekWorkLogShort = new TreeMap<Issue, Map<Date, Long>>(new IssueKeyComparator());
    private final Map<ApplicationUser, Map<Date, Long>> userWorkLogShort = new TreeMap<>(new UserComparator());
    private final Map<Long, Long> weekTotalTimeSpents = new Hashtable<>();
    private final Map<ApplicationUser, Map<Issue, Long>> userTotalTimeSpents = new Hashtable<>();
    private final Map<Project, Map<Date, Long>> projectTimeSpents = new Hashtable<>();
    private final Map<Project, Map<String, Map<Date, Long>>> projectGroupedByFieldTimeSpents = new Hashtable<>();
}
