package com.scn.jira.worklog.core.scnwl;

import com.atlassian.core.ofbiz.util.CoreTransactionUtil;
import com.atlassian.instrumentation.utils.dbc.Assertions;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogImpl;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.Lists;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.core.wl.ExtendedWorklogManagerImpl;
import org.ofbiz.core.entity.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;
import java.util.*;

import static org.ofbiz.core.entity.EntityOperator.*;

@ExportAsService({OfBizScnWorklogStore.class})
@Named("ofBizScnWorklogStore")
public class OfBizScnWorklogStore implements IScnWorklogStore {
    private final OfBizDelegator ofBizDelegator;
    private final IssueManager issueManager;
    private final ProjectRoleManager projectRoleManager;
    private final WorklogManager worklogManager;
    private final ExtendedWorklogManagerImpl extWorklogManager;
    private final IScnProjectSettingsManager scnProjectSettingsManager;

    @Inject
    public OfBizScnWorklogStore(OfBizDelegator ofBizDelegator,
                                @ComponentImport IssueManager issueManager,
                                @ComponentImport ProjectRoleManager projectRoleManager,
                                ExtendedWorklogManagerImpl extendedWorklogManager,
                                IScnProjectSettingsManager scnProjectSettingsManager) {
        this.ofBizDelegator = ofBizDelegator;
        this.issueManager = issueManager;
        this.projectRoleManager = projectRoleManager;
        this.worklogManager = ComponentAccessor.getComponent(WorklogManager.class);
        this.extWorklogManager = extendedWorklogManager;
        this.scnProjectSettingsManager = scnProjectSettingsManager;
    }

    protected IScnWorklog update(IScnWorklog worklog) throws DataAccessException {
        GenericValue worklogGV = ofBizDelegator.findByPrimaryKey(SCN_WORKLOG_ENTITY, MapBuilder.build("id", worklog.getId()));
        if (worklogGV == null)
            throw new DataAccessException("Could not find original worklog entity to update.");

        Map<String, Object> fields = createParamMap(worklog);
        worklogGV.setFields(fields);
        try {
            worklogGV.store();
        } catch (GenericEntityException e) {
            throw new DataAccessException(e);
        }

        return convertToWorklog(worklogGV);
    }

    protected Worklog updateLinkedWorklog(Worklog worklog, String worklogTypeId) throws DataAccessException {
        GenericValue worklogGV = ofBizDelegator.findByPrimaryKey(WORKLOG_ENTITY, MapBuilder.build("id", worklog.getId()));
        if (worklogGV == null)
            throw new DataAccessException("Could not find original worklog entity to update.");

        Map<String, Object> fields = createLinkedParamMap(worklog);
        worklogGV.setFields(fields);
        try {
            worklogGV.store();
        } catch (GenericEntityException e) {
            throw new DataAccessException(e);
        }

        Worklog linkedWorklog = convertToLinkedWorklog(worklog.getIssue(), worklogGV);
        this.extWorklogManager.updateExtWorklogType(linkedWorklog.getId(), worklogTypeId);
        return linkedWorklog;
    }

    public IScnWorklog update(IScnWorklog worklog, boolean isLinkedWL) throws DataAccessException {
        IScnWorklog updatedWorklog;
        boolean isWLAutoCopyBlocked = isWLAutoCopyBlocked(worklog);
        if (isLinkedWL && !isWLAutoCopyBlocked) {
            try {
                boolean transactionStarted = CoreTransactionUtil.begin();
                try {
                    Worklog linkedWorklog = new WorklogImpl(
                        this.worklogManager,
                        worklog.getIssue(),
                        worklog.getLinkedWorklog() != null ? worklog.getLinkedWorklog().getId() : null,
                        worklog.getAuthorKey(),
                        worklog.getComment(),
                        worklog.getStartDate(),
                        worklog.getGroupLevel(),
                        worklog.getRoleLevelId(),
                        worklog.getTimeSpent(),
                        worklog.getUpdateAuthorKey(),
                        worklog.getCreated(),
                        worklog.getUpdated());

                    if (worklog.getLinkedWorklog() == null)
                        linkedWorklog = createLinkedWorklog(linkedWorklog, worklog.getWorklogTypeId());
                    else
                        linkedWorklog = updateLinkedWorklog(linkedWorklog, worklog.getWorklogTypeId());

                    worklog.setLinkedWorklog(linkedWorklog);
                    updatedWorklog = update(worklog);
                    CoreTransactionUtil.commit(transactionStarted);
                } catch (GenericTransactionException e) {
                    CoreTransactionUtil.rollback(transactionStarted);
                    throw new DataAccessException("Error occurred while updating linked worklog.", e);
                }
            } catch (GenericTransactionException e) {
                throw new DataAccessException("Error occurred while rolling back transaction.", e);
            }
        } else {
            updatedWorklog = update(worklog);
            if (worklog.getLinkedWorklog() != null && !isWLAutoCopyBlocked) {
                final Long id = worklog.getLinkedWorklog().getId();
                ofBizDelegator.removeByAnd(WORKLOG_ENTITY, MapBuilder.build("id", id));
                this.extWorklogManager.deleteExtWorklogType(id);
            }
        }
        return updatedWorklog;
    }

    protected IScnWorklog create(IScnWorklog worklog) throws DataAccessException {
        Map<String, Object> fields = createParamMap(worklog);
        GenericValue worklogGV = ofBizDelegator.createValue(SCN_WORKLOG_ENTITY, fields);
        return convertToWorklog(worklogGV);
    }

    protected Worklog createLinkedWorklog(Worklog worklog, String worklogTypeId) throws DataAccessException {
        final Map<String, Object> fields = createLinkedParamMap(worklog);
        final GenericValue worklogGV = ofBizDelegator.createValue(WORKLOG_ENTITY, fields);
        final Worklog linkedWorklog = convertToLinkedWorklog(worklog.getIssue(), worklogGV);
        this.extWorklogManager.createExtWorklogType(linkedWorklog, worklogTypeId);
        return linkedWorklog;
    }

    public IScnWorklog create(IScnWorklog worklog, boolean isLinkedWl) throws DataAccessException {
        IScnWorklog newWorklog;
        boolean isWLAutoCopyBlocked = isWLAutoCopyBlocked(worklog);
        if (isLinkedWl && !isWLAutoCopyBlocked) {
            try {
                boolean transactionStarted = CoreTransactionUtil.begin();
                try {
                    Worklog linkedWorklog = new WorklogImpl(
                        this.worklogManager,
                        worklog.getIssue(),
                        null,
                        worklog.getAuthorKey(),
                        worklog.getComment(),
                        worklog.getStartDate(),
                        worklog.getGroupLevel(),
                        worklog.getRoleLevelId(),
                        worklog.getTimeSpent(),
                        worklog.getUpdateAuthorKey(),
                        worklog.getCreated(),
                        worklog.getUpdated());
                    linkedWorklog = createLinkedWorklog(linkedWorklog, worklog.getWorklogTypeId());

                    worklog.setLinkedWorklog(linkedWorklog);
                    newWorklog = create(worklog);
                    CoreTransactionUtil.commit(transactionStarted);
                } catch (Exception e) {
                    CoreTransactionUtil.rollback(transactionStarted);
                    throw new DataAccessException("Error occurred while creating linked worklog.", e);
                }
            } catch (GenericTransactionException e) {
                throw new DataAccessException("Error occurred while rolling back transaction.", e);
            }
        } else {
            newWorklog = create(worklog);
        }
        return newWorklog;
    }

    protected boolean delete(Long worklogId) {
        if (worklogId == null) {
            throw new DataAccessException("Cannot remove a worklog with id null.");
        }
        int numRemoved = ofBizDelegator.removeByAnd(SCN_WORKLOG_ENTITY, MapBuilder.build("id", worklogId));
        return numRemoved == 1;
    }

    public boolean delete(Long worklogId, boolean isLinkedWl) {
        if (worklogId == null) {
            throw new DataAccessException("Cannot remove a worklog with id null.");
        }
        boolean result = false;

        boolean isWLAutoCopyBlocked = Optional.ofNullable(getById(worklogId)).flatMap(worklog -> Optional.of(isWLAutoCopyBlocked(worklog))).orElse(false);
        if (isLinkedWl && !isWLAutoCopyBlocked) {
            try {
                boolean transactionStarted = CoreTransactionUtil.begin();
                try {
                    final IScnWorklog worklog = getById(worklogId);
                    if (worklog != null) {
                        if (worklog.getLinkedWorklog() != null) {
                            final Long id = worklog.getLinkedWorklog().getId();
                            ofBizDelegator.removeByAnd(WORKLOG_ENTITY, MapBuilder.build("id", id));
                            this.extWorklogManager.deleteExtWorklogType(id);
                        }
                        result = delete(worklogId);
                    }
                    CoreTransactionUtil.commit(transactionStarted);
                } catch (Exception e) {
                    CoreTransactionUtil.rollback(transactionStarted);
                    throw new DataAccessException("Error occurred while removing linked worklog.", e);
                }
            } catch (GenericTransactionException e) {
                throw new DataAccessException("Error occurred while rolling back transaction.", e);
            }
        } else {
            result = delete(worklogId);
        }
        return result;
    }

    public int deleteAllByIssueId(Long issueId) {
        Assertions.notNull("Issue Id", issueId);

        return ofBizDelegator.removeByAnd(SCN_WORKLOG_ENTITY, MapBuilder.build("issue", issueId));
    }

    protected Map<String, Object> createParamMap(IScnWorklog worklog) {
        Assertions.notNull("worklog", worklog);
        Assertions.notNull("issue", worklog.getIssue());

        Map<String, Object> fields = createLinkedParamMap(worklog);
        fields.put("linkedWorklog", worklog.getLinkedWorklog() != null ? worklog.getLinkedWorklog().getId() : null);
        fields.put("worklogtype", worklog.getWorklogTypeId());
        return fields;
    }

    protected Map<String, Object> createLinkedParamMap(Worklog worklog) {
        Assertions.notNull("worklog", worklog);
        Assertions.notNull("issue", worklog.getIssue());

        Map<String, Object> fields = new HashMap<>();
        fields.put("issue", worklog.getIssue().getId());
        fields.put("author", worklog.getAuthorKey());
        fields.put("updateauthor", worklog.getUpdateAuthorKey());
        fields.put("body", worklog.getComment());
        fields.put("grouplevel", worklog.getGroupLevel());
        fields.put("rolelevel", worklog.getRoleLevelId());
        fields.put("timeworked", worklog.getTimeSpent());
        fields.put("startdate", new Timestamp(worklog.getStartDate().getTime()));
        fields.put("created", new Timestamp(worklog.getCreated().getTime()));
        fields.put("updated", new Timestamp(worklog.getUpdated().getTime()));
        return fields;
    }

    public IScnWorklog getById(Long id) throws DataAccessException {
        GenericValue worklogGV = ofBizDelegator.findByPrimaryKey(SCN_WORKLOG_ENTITY, MapBuilder.build("id", id));
        return convertToWorklog(worklogGV);
    }

    public List<IScnWorklog> getByIssue(Issue issue) throws DataAccessException {
        Assertions.notNull("issue", issue);

        List<GenericValue> worklogGVs = ofBizDelegator.findByAnd(SCN_WORKLOG_ENTITY, MapBuilder.build("issue", issue.getId()), Lists.newArrayList("created ASC"));
        return convertToWorklog(worklogGVs);
    }

    public List<IScnWorklog> getByProject(Project project) throws DataAccessException {
        Assertions.notNull("project", project);

        List<GenericValue> worklogGVs = ofBizDelegator.findByAnd("ScnWorklogByProjectView", MapBuilder.build("projectId", project.getId()), Lists.newArrayList("created ASC"));
        return convertToWorklog(worklogGVs);
    }

    public List<IScnWorklog> getByProjectBetweenDates(Project project, Date startDate, Date endDate) throws DataAccessException {
        Assertions.notNull("project", project);

        List<EntityCondition> conditions = Lists.newArrayList();
        conditions.add(new EntityExpr("startdate", LESS_THAN_EQUAL_TO, new java.sql.Date(endDate.getTime())));
        conditions.add(new EntityExpr("startdate", GREATER_THAN_EQUAL_TO, new java.sql.Date(startDate.getTime())));
        conditions.add(new EntityExpr("projectId", EQUALS, project.getId()));
        EntityCondition conditionList = new EntityConditionList(conditions, EntityOperator.AND);

        List<GenericValue> worklogGVs = ofBizDelegator.findByCondition("ScnWorklogByProjectView", conditionList, null, Lists.newArrayList("created ASC"));
        return convertToWorklog(worklogGVs);
    }

    public int swapWorklogGroupRestriction(String groupName, String swapGroup) throws DataAccessException {
        Assertions.notNull("group name", groupName);
        Assertions.notNull("swap group name", swapGroup);

        return ofBizDelegator.bulkUpdateByAnd(SCN_WORKLOG_ENTITY, MapBuilder.build("grouplevel", swapGroup), MapBuilder.build("grouplevel", groupName));
    }

    public long getCountForWorklogsRestrictedByGroup(String groupName) throws DataAccessException {
        Assertions.notNull("group name", groupName);

        EntityCondition condition = new EntityFieldMap(MapBuilder.build("grouplevel", groupName), EntityOperator.AND);
        List<GenericValue> worklogCount = ofBizDelegator.findByCondition("ScnWorklogCount", condition, Lists.newArrayList("count"), Collections.<String>emptyList());
        if (worklogCount != null && worklogCount.size() == 1) {
            GenericValue worklogCountGV = worklogCount.get(0);
            return worklogCountGV.getLong("count");
        } else {
            throw new DataAccessException("Unable to access the count for the ScnWorklog table");
        }
    }

    public boolean deleteLinkedWorklog(Long linkedWorklogId) throws DataAccessException {
        Assertions.notNull("worklog id", linkedWorklogId);

        int numRemoved = 0;
        try {
            boolean transactionStarted = CoreTransactionUtil.begin();
            try {
                numRemoved = ofBizDelegator.removeByAnd(WORKLOG_ENTITY, MapBuilder.build("id", linkedWorklogId));

                List<GenericValue> list = ofBizDelegator.findByAnd(SCN_WORKLOG_ENTITY, MapBuilder.build("linkedWorklog", linkedWorklogId));
                for (GenericValue scnWorklogGV : list) {
                    scnWorklogGV.set("linkedWorklog", null);
                    scnWorklogGV.store();
                }
                CoreTransactionUtil.commit(transactionStarted);
            } catch (Exception e) {
                CoreTransactionUtil.rollback(transactionStarted);
                throw new DataAccessException("Error occurred while removing linked worklog.", e);
            }
        } catch (GenericTransactionException e) {
            throw new DataAccessException("Error occurred while rolling back transaction.", e);
        }
        return numRemoved == 1;
    }

    public List<IScnWorklog> getScnWorklogsByType(String worklogTypeId) throws DataAccessException {
        List<GenericValue> list = ofBizDelegator.findByAnd(SCN_WORKLOG_ENTITY, MapBuilder.build("worklogtype", worklogTypeId));
        return convertToWorklog(list);
    }

    protected List<IScnWorklog> convertToWorklog(List<GenericValue> gvs) {
        List<IScnWorklog> worklogs = new ArrayList<>();
        for (GenericValue gv : gvs)
            worklogs.add(convertToWorklog(gv));
        return worklogs;
    }

    protected IScnWorklog convertToWorklog(GenericValue gv) {
        if (gv == null) return null;

        Timestamp startDateTS = gv.getTimestamp("startdate");
        Timestamp createdTS = gv.getTimestamp("created");
        Timestamp updatedTS = gv.getTimestamp("updated");
        IScnWorklog worklog = new ScnWorklogImpl(
            this.projectRoleManager,
            getIssueForId(gv.getLong("issue")),
            gv.getLong("id"),
            gv.getString("author"),
            gv.getString("body"),
            startDateTS != null ? new Date(startDateTS.getTime()) : null,
            gv.getString("grouplevel"),
            gv.getLong("rolelevel"),
            gv.getLong("timeworked"),
            gv.getString("updateauthor"),
            createdTS != null ? new Date(createdTS.getTime()) : null,
            updatedTS != null ? new Date(updatedTS.getTime()) : null,
            gv.getString("worklogtype"));

        Long linkedWorklogId = gv.getLong("linkedWorklog");
        worklog.setLinkedWorklog(linkedWorklogId == null ? null : getLinkedWorklogById(linkedWorklogId));

        return worklog;
    }

    protected Worklog convertToLinkedWorklog(Issue issue, GenericValue gv) {
        Timestamp startDateTS = gv.getTimestamp("startdate");
        Timestamp createdTS = gv.getTimestamp("created");
        Timestamp updatedTS = gv.getTimestamp("updated");
        return new WorklogImpl(
            this.worklogManager,
            issue,
            gv.getLong("id"),
            gv.getString("author"),
            gv.getString("body"),
            startDateTS != null ? new Date(startDateTS.getTime()) : null,
            gv.getString("grouplevel"),
            gv.getLong("rolelevel"),
            gv.getLong("timeworked"),
            gv.getString("updateauthor"),
            createdTS != null ? new Date(createdTS.getTime()) : null,
            updatedTS != null ? new Date(updatedTS.getTime()) : null);
    }

    protected Issue getIssueForId(Long issueId) {
        return issueManager.getIssueObject(issueId);
    }

    protected Worklog getLinkedWorklogById(Long linkedWorklogId) {
        return this.worklogManager.getById(linkedWorklogId);
    }

    private boolean isWLAutoCopyBlocked(IScnWorklog wl) {
        if (wl == null)
            return false;
        Date wlWorklogBlockingDate = this.scnProjectSettingsManager.getWLWorklogBlockingDate(wl.getIssue().getProjectId());

        return wlWorklogBlockingDate != null && !wl.getStartDate().after(wlWorklogBlockingDate);
    }
}
