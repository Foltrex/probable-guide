package com.scn.jira.automation.impl.domain.service;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogImpl;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.transaction.Transaction;
import com.atlassian.jira.transaction.Txn;
import com.atlassian.jira.user.util.UserManager;
import com.google.common.collect.Lists;
import com.scn.jira.automation.api.domain.service.ScnBIService;
import com.scn.jira.automation.api.domain.service.WorklogContextService;
import com.scn.jira.automation.impl.domain.dto.WorklogDto;
import com.scn.jira.automation.impl.domain.dto.WorklogTypeDto;
import com.scn.jira.automation.impl.domain.entity.AutoTT;
import com.scn.jira.common.exception.InternalRuntimeException;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import com.scn.jira.worklog.core.scnwl.ScnWorklogImpl;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.ExtendedWorklogManager;
import com.scn.jira.worklog.core.wl.WorklogType;
import com.scn.jira.worklog.scnwl.IScnWorklogService;
import lombok.RequiredArgsConstructor;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ofbiz.core.entity.EntityOperator.EQUALS;
import static org.ofbiz.core.entity.EntityOperator.GREATER_THAN_EQUAL_TO;
import static org.ofbiz.core.entity.EntityOperator.LESS_THAN_EQUAL_TO;

@Service
@RequiredArgsConstructor
public class WorklogContextServiceImpl implements WorklogContextService {
    private final ExtendedConstantsManager extendedConstantsManager;
    private final OfBizDelegator ofBizDelegator;
    private final IssueManager issueManager;
    private final ProjectRoleManager projectRoleManager;
    private final JiraAuthenticationContext authenticationContext;
    private final IScnProjectSettingsManager projectSettingsManager;
    private final IScnWorklogService scnDefaultWorklogService;
    private final WorklogManager worklogManager;
    private final ExtendedWorklogManager extendedWorklogManager;
    private final UserManager userManager;
    private final ScnBIService scnBIService;

    @Override
    public List<WorklogTypeDto> getAllWorklogTypes() {
        Collection<WorklogType> worklogTypes = extendedConstantsManager.getWorklogTypeObjects();
        return worklogTypes.stream()
            .map(value -> new WorklogTypeDto(value.getId(), value.getName()))
            .collect(Collectors.toList());
    }

    @Override
    public void createWorklog(@Nonnull WorklogDto worklog) {
        Issue issue = issueManager.getIssueObject(worklog.getIssueId());
        Worklog newWorklog = new WorklogImpl(
            worklogManager, issue, null, worklog.getAuthorKey(), worklog.getWorklogBody(),
            worklog.getStartDate(), null, null, worklog.getTimeWorked());
        Long oldEstimate = issue.getEstimate() == null ? 0L : issue.getEstimate();
        Long timeSpend = worklog.getTimeWorked() == null ? 0L : worklog.getTimeWorked();
        Long newEstimate = oldEstimate <= timeSpend ? 0L : oldEstimate - timeSpend;
        Worklog createdWorklog = worklogManager.create(authenticationContext.getLoggedInUser(), newWorklog, newEstimate, false);
        if (createdWorklog != null) {
            extendedWorklogManager.createExtWorklogType(createdWorklog, worklog.getWorklogTypeId());
        }
    }

    @Override
    public void deleteWorklogById(Long id) {
        Worklog worklog = worklogManager.getById(id);
        if (worklog != null) {
            Long oldEstimate = worklog.getIssue().getEstimate() == null ? 0L : worklog.getIssue().getEstimate();
            Long timeSpend = worklog.getTimeSpent() == null ? 0L : worklog.getTimeSpent();
            Long newEstimate = oldEstimate + timeSpend;
            worklogManager.delete(authenticationContext.getLoggedInUser(), worklog, newEstimate, false);
            extendedWorklogManager.deleteExtWorklogType(id);
        }
    }

    @Override
    public void doAutoTimeTracking(@Nonnull AutoTT autoTT, LocalDate to) {
        Map<Date, ScnBIService.DayType> userCalendar = scnBIService.getUserCalendar(autoTT.getUsername(), autoTT.getStartDate().toLocalDateTime().toLocalDate(), to);
        Set<Date> workedDays = getWorkedDays(autoTT.getUserKey(), autoTT.getStartDate().toLocalDateTime().toLocalDate(), to);
        Transaction txn = Txn.begin();
        try {
            userCalendar.forEach((date, dayType) -> {
                if (dayType.equals(ScnBIService.DayType.WORKING) && !workedDays.contains(date)) {
                    createScnWorklog(autoTT, date);
                }
            });
            txn.commit();
        } catch (Exception e) {
            throw new InternalRuntimeException(e);
        } finally {
            txn.finallyRollbackIfNotCommitted();
        }
    }

    private Set<Date> getWorkedDays(String userKey, @Nonnull LocalDate from, @Nonnull LocalDate to) {
        List<EntityCondition> conditions = Lists.newArrayList();
        conditions.add(new EntityExpr("startdate", GREATER_THAN_EQUAL_TO, Timestamp.valueOf(from.atStartOfDay())));
        conditions.add(new EntityExpr("startdate", LESS_THAN_EQUAL_TO, Timestamp.valueOf(to.atStartOfDay().plusDays(1).minusNanos(1))));
        conditions.add(new EntityExpr("author", EQUALS, userKey));
        EntityCondition conditionList = new EntityConditionList(conditions, EntityOperator.AND);

        return ofBizDelegator.findByCondition("ScnWorklog", conditionList, Lists.newArrayList("startdate", "timeworked"))
            .stream().filter(gv -> gv.getTimestamp("startdate") != null && gv.getLong("timeworked") != null && gv.getLong("timeworked") > 0L)
            .map(gv -> Date.from(
                gv.getTimestamp("startdate").toLocalDateTime().toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()
            ))
            .collect(Collectors.toSet());
    }

    private void createScnWorklog(@Nonnull AutoTT autoTT, Date date) {
        Issue issue = issueManager.getIssueObject(autoTT.getIssueId());
        if (issue != null) {
            IScnWorklog worklog = new ScnWorklogImpl(projectRoleManager, issue, null, autoTT.getUserKey(),
                "Auto-generated worklog by ScienceSoft Plugin for Jira.", date, null, null,
                autoTT.getRatedTime(),
                autoTT.getWorklogTypeId() == null ? "0" : autoTT.getWorklogTypeId());
            boolean isAutoCopy = isWlAutoCopy(autoTT);
            IScnWorklog createdWorklog = scnDefaultWorklogService.createAndAutoAdjustRemainingEstimate(
                new JiraServiceContextImpl(userManager.getUserByKey(autoTT.getUserKey())),
                worklog, true, isAutoCopy);
            if (createdWorklog == null) {
                throw new InternalRuntimeException("Error when creating auto worklog for user " + autoTT.getUsername());
            }
        }
    }

    private boolean isWlAutoCopy(@Nonnull AutoTT autoTT) {
        return projectSettingsManager.isWLAutoCopyEnabled(autoTT.getProjectId())
            && (autoTT.getWorklogTypeId() == null ?
            projectSettingsManager.isUnspecifiedWLTypeAutoCopyEnabled(autoTT.getProjectId())
            : projectSettingsManager.getWorklogTypes(autoTT.getProjectId()).stream()
            .anyMatch(worklogType -> worklogType.getId().equals(autoTT.getWorklogTypeId()))
        );
    }
}
