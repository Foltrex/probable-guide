package com.scn.jira.worklog.core.scnwl;

import com.atlassian.jira.user.ApplicationUser;

/**
 * Created by IntelliJ IDEA.
 * User: Khadarovich
 * Date: 11.08.2010
 * Time: 16:23:00
 * To change this template use File | Settings | File Templates.
 */
public interface IScnTimeTrackingIssueManager {
    public abstract void updateIssueOnWorklogCreate(ApplicationUser user, IScnWorklog worklog, Long long1, boolean flag);

    public abstract void updateIssueOnWorklogUpdate(ApplicationUser user, IScnWorklog worklog, IScnWorklog worklog1, Long long1, Long long2, boolean flag);

    public abstract void updateIssueOnWorklogDelete(ApplicationUser user, IScnWorklog worklog, Long long1, boolean flag);    
}
