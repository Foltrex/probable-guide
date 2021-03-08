package com.scn.jira.worklog.core.scnwl;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.worklog.TimeTrackingIssueUpdater;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.dbc.Assertions;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;

@Named("defaultScnWorklogManager")
public class DefaultScnWorklogManager implements IScnWorklogManager {
    private final OfBizScnWorklogStore worklogStore;
    private final TimeTrackingIssueUpdater timeTrackingIssueUpdater;
    private final IScnTimeTrackingIssueManager scnTimeTrackingIssueManager;
    private final IScnProjectSettingsManager scnProjectSettingsManager;

    @Inject
    public DefaultScnWorklogManager(OfBizScnWorklogStore worklogStore,
                                    TimeTrackingIssueUpdater timeTrackingIssueUpdater,
                                    ScnTimeTrackingIssueManager scnTimeTrackingIssueManager,
                                    IScnProjectSettingsManager scnProjectSettingsManager) {
        this.worklogStore = worklogStore;
        this.timeTrackingIssueUpdater = timeTrackingIssueUpdater;
        this.scnTimeTrackingIssueManager = scnTimeTrackingIssueManager;
        this.scnProjectSettingsManager = scnProjectSettingsManager;
    }

    public boolean delete(ApplicationUser user, IScnWorklog worklog, Long newEstimate, Long newLinkedEstimate, boolean dispatchEvent, boolean isLinkedWL) throws DataAccessException {
        validateWorklog(worklog, false);
        final Worklog linkedWorklog = worklog.getLinkedWorklog();
        boolean result = this.worklogStore.delete(worklog.getId(), isLinkedWL);
        boolean isWLAutoCopyBlocked = isWLAutoCopyBlocked(worklog);
        if (isLinkedWL && linkedWorklog != null && !isWLAutoCopyBlocked) {
            // send event about created linked WL and update time tracking record
            this.timeTrackingIssueUpdater.updateIssueOnWorklogDelete(user, linkedWorklog, newLinkedEstimate, dispatchEvent);
        }
        // update timeSpent* record
        scnTimeTrackingIssueManager.updateIssueOnWorklogDelete(user, worklog, newEstimate, dispatchEvent);

        return result;
    }

    public IScnWorklog create(ApplicationUser user, IScnWorklog worklog, Long newEstimate, Long newLinkedEstimate, boolean dispatchEvent, boolean isLinkedWL) throws DataAccessException {
        validateWorklog(worklog, true);
        final IScnWorklog newWorklog = this.worklogStore.create(worklog, isLinkedWL);
        boolean isWLAutoCopyBlocked = isWLAutoCopyBlocked(worklog);
        if (isLinkedWL && newWorklog.getLinkedWorklog() != null && !isWLAutoCopyBlocked) {
            final Worklog newLinkedWorklog = newWorklog.getLinkedWorklog();
            // send event about created linked WL and update time tracking record
            this.timeTrackingIssueUpdater.updateIssueOnWorklogCreate(user, newLinkedWorklog, newLinkedEstimate, dispatchEvent);
        }
        // update timeSpent* record
        scnTimeTrackingIssueManager.updateIssueOnWorklogCreate(user, worklog, newEstimate, dispatchEvent);

        return newWorklog;
    }

    public IScnWorklog update(ApplicationUser user, IScnWorklog worklog, Long newEstimate, Long newLinkedEstimate, boolean dispatchEvent, boolean isLinkedWL) throws DataAccessException {
        validateWorklog(worklog, false);
        final IScnWorklog originalWorklog = getById(worklog.getId());
        if (originalWorklog == null) {
            throw new DataAccessException("Unable to find a worklog in the datastore for the provided id: '" + worklog.getId() + "'");
        }
        final Worklog originalLinkedWorklog = originalWorklog.getLinkedWorklog();
        IScnWorklog newWorklog = this.worklogStore.update(worklog, isLinkedWL);
        boolean isWLAutoCopyBlocked = isWLAutoCopyBlocked(worklog);
        if (isLinkedWL && originalLinkedWorklog != null && !isWLAutoCopyBlocked) {
            // send event about created linked WL and update time tracking record
            final Long originalTimeSpent = originalLinkedWorklog.getTimeSpent();
            this.timeTrackingIssueUpdater.updateIssueOnWorklogUpdate(user, originalLinkedWorklog, newWorklog.getLinkedWorklog(),
                originalTimeSpent, newLinkedEstimate, dispatchEvent);
        } else if (originalLinkedWorklog != null && !isWLAutoCopyBlocked) {
            this.timeTrackingIssueUpdater.updateIssueOnWorklogDelete(user, originalLinkedWorklog,
                newLinkedEstimate, dispatchEvent);
        } else if (isLinkedWL && !isWLAutoCopyBlocked) {
            this.timeTrackingIssueUpdater.updateIssueOnWorklogUpdate(user, null, newWorklog.getLinkedWorklog(),
                0L, newLinkedEstimate, dispatchEvent);
        }
        // update timeSpent* record
        scnTimeTrackingIssueManager.updateIssueOnWorklogUpdate(user, originalWorklog, newWorklog, originalWorklog.getTimeSpent(), newEstimate, dispatchEvent);

        return newWorklog;
    }

    public IScnWorklog update(IScnWorklog worklog) throws DataAccessException {
        return worklogStore.update(worklog, false);
    }

    public IScnWorklog getById(Long id) throws DataAccessException {
        return this.worklogStore.getById(id);
    }

    public List<IScnWorklog> getByIssue(Issue issue) throws DataAccessException {
        Assertions.notNull("issue", issue);

        return this.worklogStore.getByIssue(issue);
    }

    public List<IScnWorklog> getByProjectBetweenDates(Project project, Date startDate, Date endDate) throws DataAccessException {
        Assertions.notNull("project", project);

        return this.worklogStore.getByProjectBetweenDates(project, startDate, endDate);
    }

    public List<IScnWorklog> getByProject(Project project) throws DataAccessException {
        Assertions.notNull("project", project);

        return this.worklogStore.getByProject(project);
    }

    public int swapWorklogGroupRestriction(String groupName, String swapGroup) throws DataAccessException {
        Assertions.notNull("groupName", groupName);
        Assertions.notNull("swapGroup", swapGroup);

        return this.worklogStore.swapWorklogGroupRestriction(groupName, swapGroup);
    }

    public long getCountForWorklogsRestrictedByGroup(String groupName) throws DataAccessException {
        Assertions.notNull("groupName", groupName);

        return this.worklogStore.getCountForWorklogsRestrictedByGroup(groupName);
    }

    public void validateWorklog(IScnWorklog worklog, boolean create) {
        Assertions.notNull("IScnWorklog", worklog);
        Assertions.notNull("worklog issue", worklog.getIssue());

        if ((create) || (worklog.getId() != null))
            return;
        throw new IllegalArgumentException("Can not modify a worklog with a null id.");
    }

    public List<IScnWorklog> getScnWorklogsByType(String worklogTypeId) throws DataAccessException {
        return this.worklogStore.getScnWorklogsByType(worklogTypeId);
    }

    private boolean isWLAutoCopyBlocked(IScnWorklog wl) {
        if (wl == null)
            return false;
        Date wlWorklogBlockingDate = this.scnProjectSettingsManager.getWLWorklogBlockingDate(wl.getIssue().getProjectId());

        return wlWorklogBlockingDate != null && !wl.getStartDate().after(wlWorklogBlockingDate);
    }
}
