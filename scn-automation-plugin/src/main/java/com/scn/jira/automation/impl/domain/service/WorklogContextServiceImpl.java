package com.scn.jira.automation.impl.domain.service;

import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogImpl;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.google.common.collect.Lists;
import com.scn.jira.automation.api.domain.service.JiraContextService;
import com.scn.jira.automation.api.domain.service.WorklogContextService;
import com.scn.jira.automation.impl.domain.dto.AutoTTDto;
import com.scn.jira.automation.impl.domain.dto.WorklogDto;
import com.scn.jira.automation.impl.domain.dto.WorklogTypeDto;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import com.scn.jira.worklog.core.scnwl.ScnWorklogImpl;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.ExtendedWorklogManager;
import com.scn.jira.worklog.core.wl.WorklogType;
import com.scn.jira.worklog.scnwl.IScnWorklogService;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ofbiz.core.entity.EntityOperator.*;

@Service
@ExportAsService(WorklogContextService.class)
public class WorklogContextServiceImpl implements WorklogContextService {
    private final ExtendedConstantsManager extendedConstantsManager;
    private final OfBizDelegator ofBizDelegator;
    private final IssueManager issueManager;
    private final ProjectRoleManager projectRoleManager;
    private final JiraContextService jiraContextService;
    private final IScnProjectSettingsManager projectSettingsManager;
    private final IScnWorklogService scnDefaultWorklogService;
    private final JiraDurationUtils jiraDurationUtils;
    private final WorklogManager worklogManager;
    private final ExtendedWorklogManager extendedWorklogManager;

    @Autowired
    public WorklogContextServiceImpl(ExtendedConstantsManager extendedConstantsManager, OfBizDelegator ofBizDelegator,
                                     IssueManager issueManager, ProjectRoleManager projectRoleManager,
                                     JiraContextService jiraContextService, IScnProjectSettingsManager projectSettingsManager,
                                     IScnWorklogService scnDefaultWorklogService, ExtendedWorklogManager extendedWorklogManager,
                                     @Qualifier("overridedWorklogManager") WorklogManager worklogManager) {
        this.extendedConstantsManager = extendedConstantsManager;
        this.ofBizDelegator = ofBizDelegator;
        this.issueManager = issueManager;
        this.projectRoleManager = projectRoleManager;
        this.jiraContextService = jiraContextService;
        this.projectSettingsManager = projectSettingsManager;
        this.scnDefaultWorklogService = scnDefaultWorklogService;
        this.worklogManager = worklogManager;
        this.extendedWorklogManager = extendedWorklogManager;
        this.jiraDurationUtils = ComponentAccessor.getComponent(JiraDurationUtils.class);
    }

    @Override
    public WorklogTypeDto getWorklogType(String id) {
        WorklogType worklogType = extendedConstantsManager.getWorklogTypeObject(id);
        return worklogType == null ? null : new WorklogTypeDto(worklogType.getId(), worklogType.getName());
    }

    @Override
    public List<WorklogTypeDto> getAllWorklogTypes() {
        Collection<WorklogType> worklogTypes = extendedConstantsManager.getWorklogTypeObjects();
        return worklogTypes.stream()
            .map(value -> new WorklogTypeDto(value.getId(), value.getName()))
            .collect(Collectors.toList());
    }

    @Override
    public String getFormattedTime(Long time) {
        return jiraDurationUtils.getShortFormattedDuration(time, jiraContextService.getLocale());
    }

    @Override
    public Long getParsedTime(String formattedTime) {
        try {
            return jiraDurationUtils.parseDuration(formattedTime, jiraContextService.getLocale());
        } catch (InvalidDurationException e) {
            return null;
        }
    }

    @Override
    public boolean isValidFormattedTime(String formattedTime) {
        try {
            jiraDurationUtils.parseDuration(formattedTime, jiraContextService.getLocale());
        } catch (InvalidDurationException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isBlankFormattedTime(String formattedTime) {
        Long time = 0L;
        try {
            time = jiraDurationUtils.parseDuration(formattedTime, jiraContextService.getLocale());
        } catch (InvalidDurationException ignored) {
        }
        return time == null || time == 0L;
    }

    @Override
    public Set<Date> getWorkedDays(String userKey, @Nonnull Date from, @Nonnull Date to) {
        List<EntityCondition> conditions = Lists.newArrayList();
        conditions.add(new EntityExpr("startdate", GREATER_THAN_EQUAL_TO, new Timestamp(from.getTime())));
        conditions.add(new EntityExpr("startdate", LESS_THAN_EQUAL_TO,
            Timestamp.valueOf(
                to.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().plusDays(1).minusNanos(1)
            )));
        conditions.add(new EntityExpr("author", EQUALS, userKey));
        EntityCondition conditionList = new EntityConditionList(conditions, EntityOperator.AND);

        return ofBizDelegator.findByCondition("ScnWorklog", conditionList, Lists.newArrayList("startdate", "timeworked"))
            .stream().filter(gv -> gv.getTimestamp("startdate") != null && gv.getLong("timeworked") != null && gv.getLong("timeworked") > 0L)
            .map(gv -> Date.from(
                gv.getTimestamp("startdate").toLocalDateTime().toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()
            ))
            .collect(Collectors.toSet());
    }

    @Override
    public void createScnWorklog(@Nonnull AutoTTDto autoTTDto, Date date) {
        Issue issue = issueManager.getIssueObject(autoTTDto.getIssue().getId());
        if (issue != null && !this.isBlankFormattedTime(autoTTDto.getRatedTime())) {
            IScnWorklog worklog = new ScnWorklogImpl(projectRoleManager, issue, null, autoTTDto.getUser().getKey(),
                "Auto-generated worklog by ScienceSoft Plugin for Jira.", date, null, null,
                this.getParsedTime(autoTTDto.getRatedTime()),
                autoTTDto.getWorklogType() == null ? "0" : autoTTDto.getWorklogType().getId());
            boolean isAutoCopy = isWlAutoCopy(autoTTDto);
            scnDefaultWorklogService.createAndAutoAdjustRemainingEstimate(
                new JiraServiceContextImpl(jiraContextService.getUser(autoTTDto.getUser().getKey())),
                worklog, true, isAutoCopy);
        }
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
        Worklog createdWorklog = worklogManager.create(jiraContextService.getCurrentUser(), newWorklog, newEstimate, false);
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
            worklogManager.delete(jiraContextService.getCurrentUser(), worklog, newEstimate, false);
            extendedWorklogManager.deleteExtWorklogType(id);
        }
    }

    private boolean isWlAutoCopy(@Nonnull AutoTTDto autoTTDto) {
        return projectSettingsManager.isWLAutoCopyEnabled(autoTTDto.getProject().getId())
            && (autoTTDto.getWorklogType() == null ?
            projectSettingsManager.isUnspecifiedWLTypeAutoCopyEnabled(autoTTDto.getProject().getId())
            : projectSettingsManager.getWorklogTypes(autoTTDto.getProject().getId()).stream()
            .anyMatch(worklogType -> worklogType.getId().equals(autoTTDto.getWorklogType().getId()))
        );
    }
}
