package com.scn.jira.worklog.core.scnwl;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.util.Assertions;

import javax.inject.Inject;
import javax.inject.Named;

@Named("scnTimeTrackingIssueManager")
public class ScnTimeTrackingIssueManager implements IScnTimeTrackingIssueManager {

    private final IScnExtendedIssueStore ofBizExtIssueStore;

    @Inject
    public ScnTimeTrackingIssueManager(IScnExtendedIssueStore ofBizExtIssueStore) {
        this.ofBizExtIssueStore = ofBizExtIssueStore;
    }

    public void updateIssueOnWorklogCreate(ApplicationUser user, IScnWorklog worklog, Long newEstimate, boolean dispatchEvent) {
        validateWorklogAndIssue(worklog);

        final IScnExtendedIssue extIssue = ofBizExtIssueStore.getByIssue(worklog.getIssue());

        Long newTotalTimeSpent;
        Long id;
        if (extIssue == null) {
            newTotalTimeSpent = worklog.getTimeSpent();
            id = null;
        } else if (extIssue.getTimeSpent() == null) {
            newTotalTimeSpent = worklog.getTimeSpent();
            id = extIssue.getId();
        } else {
            newTotalTimeSpent = new Long(extIssue.getTimeSpent() + worklog.getTimeSpent());
            id = extIssue.getId();
        }


        if (id == null) {
            final IScnExtendedIssue newExtIssue = new ScnExtendedIssue(worklog.getIssue(), id, null, newEstimate, newTotalTimeSpent);
            ofBizExtIssueStore.create(newExtIssue);
        } else {
            final IScnExtendedIssue newExtIssue = new ScnExtendedIssue(worklog.getIssue(), id,
                extIssue.getOriginalEstimate(),
                newEstimate == null ? extIssue.getEstimate() : newEstimate,
                newTotalTimeSpent);
            ofBizExtIssueStore.update(newExtIssue);
        }
    }

    public void updateIssueOnWorklogUpdate(ApplicationUser user, IScnWorklog originalWorklog, IScnWorklog newWorklog, Long originalTimeSpent, Long newEstimate, boolean dispatchEvent) {
        validateWorklogAndIssue(newWorklog);

        final IScnExtendedIssue extIssue = ofBizExtIssueStore.getByIssue(newWorklog.getIssue());

        if (extIssue != null) {
            Long newTimeSpent;
            if (extIssue.getTimeSpent() != null) {
                newTimeSpent = extIssue.getTimeSpent() - originalTimeSpent + newWorklog.getTimeSpent();
            } else {
                newTimeSpent = newWorklog.getTimeSpent();
            }

            final IScnExtendedIssue newExtIssue = new ScnExtendedIssue(newWorklog.getIssue(), extIssue.getId(),
                extIssue.getOriginalEstimate(),
                newEstimate == null ? extIssue.getEstimate() : newEstimate,
                newTimeSpent);

            ofBizExtIssueStore.update(newExtIssue);
        }
    }

    public void updateIssueOnWorklogDelete(ApplicationUser user, IScnWorklog worklog, Long newEstimate, boolean dispatchEvent) {
        validateWorklogAndIssue(worklog);

        final IScnExtendedIssue extIssue = ofBizExtIssueStore.getByIssue(worklog.getIssue());

        if (extIssue != null) {
            Long newTotalTimeSpent = 0L;
            if (extIssue.getTimeSpent() != null && (extIssue.getTimeSpent() - worklog.getTimeSpent()) > 0L) {
                newTotalTimeSpent = extIssue.getTimeSpent() - worklog.getTimeSpent();
            }

            final IScnExtendedIssue newExtIssue = new ScnExtendedIssue(worklog.getIssue(), extIssue.getId(),
                extIssue.getOriginalEstimate(),
                newEstimate == null ? extIssue.getEstimate() : newEstimate,
                newTotalTimeSpent);

            ofBizExtIssueStore.update(newExtIssue);
        }
    }

    private void validateWorklogAndIssue(IScnWorklog worklog) {
        Assertions.notNull("IScnWorklog", worklog);
        Assertions.notNull("issue", worklog.getIssue());
    }
}
