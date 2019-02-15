package com.scn.jira.worklog.panels;

import static com.atlassian.jira.issue.util.AggregateTimeTrackingBean.addAndPreserveNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.jira.plugin.viewissue.subtasks.SubTasksContextProvider;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.apache.commons.lang.StringUtils;

import com.atlassian.jira.bean.SubTask;
import com.atlassian.jira.bean.SubTaskBean;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueUtils;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.ProgressBarSystemField;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItemImpl;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutStorageException;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculator;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculatorFactory;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestSession;
import com.atlassian.jira.web.bean.TimeTrackingGraphBeanFactory;
import com.atlassian.jira.web.component.IssueTableLayoutBean;
import com.atlassian.jira.web.component.IssueTableWebComponent;
import com.atlassian.jira.web.component.TableLayoutFactory;
import com.google.common.collect.Lists;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssue;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssueStore;
import com.scn.jira.worklog.core.scnwl.ScnExtendedIssue;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.globalsettings.IGlobalSettingsManager;

@Named
public class ScnSubTasksContextProvider implements CacheableContextProvider {
	private final SubTaskManager subTaskManager;
	private final JiraAuthenticationContext authenticationContext;
	private final VelocityRequestContextFactory velocityRequestContextFactory;
	private final AggregateTimeTrackingCalculatorFactory aggregateTimeTrackingCalculatorFactory;
	private final VelocityTemplatingEngine templatingEngine;
	private final ApplicationProperties appProperties;
	private final TimeTrackingGraphBeanFactory ttGraphBeanFactory;
	private final TableLayoutFactory tableLayoutFactory = (TableLayoutFactory) ComponentAccessor
			.getComponent(TableLayoutFactory.class);
	private final PermissionManager permissionManager;
	private final IScnExtendedIssueStore eiStore;
	private final IScnProjectSettingsManager psManager;
	private final IGlobalSettingsManager gpManager;

	@Inject
	public ScnSubTasksContextProvider(@ComponentImport SubTaskManager subTaskManager, @ComponentImport JiraAuthenticationContext authenticationContext,
				  @ComponentImport VelocityRequestContextFactory velocityRequestContextFactory,
				  @ComponentImport AggregateTimeTrackingCalculatorFactory aggregateTimeTrackingCalculatorFactory,
				  @ComponentImport VelocityTemplatingEngine templatingEngine, @ComponentImport ApplicationProperties appProperties,
				  @ComponentImport TimeTrackingGraphBeanFactory ttGraphBeanFactory, @ComponentImport PermissionManager permissionManager,
				  IScnExtendedIssueStore eiStore,
				  IScnProjectSettingsManager psManager, IGlobalSettingsManager gpManager) {
//		super(subTaskManager, authenticationContext, velocityRequestContextFactory, aggregateTimeTrackingCalculatorFactory);

		this.subTaskManager = subTaskManager;
		this.authenticationContext = authenticationContext;
		this.velocityRequestContextFactory = velocityRequestContextFactory;
		this.aggregateTimeTrackingCalculatorFactory = aggregateTimeTrackingCalculatorFactory;
		this.templatingEngine = templatingEngine;
		this.appProperties = appProperties;
		this.ttGraphBeanFactory = ttGraphBeanFactory;
		this.permissionManager = permissionManager;
		this.eiStore = eiStore;
		this.psManager = psManager;
		this.gpManager = gpManager;
	}

	public void init(Map<String, String> params) throws PluginParseException {
	}

	public String getUniqueContextKey(Map<String, Object> context) {
		Issue issue = (Issue) context.get("issue");
		ApplicationUser user = authenticationContext.getUser();
		return new StringBuilder().append(issue.getId()).append("/").append((user == null) ? "" : user.getName()).toString();
		// return issue.getId() + "/" + (user != null ? user.getName() : "");
	}

	public Map<String, Object> getContextMap(Map<String, Object> context) {
		MapBuilder<String, Object> paramsBuilder = MapBuilder.newBuilder(context);
		Issue issue = (Issue) context.get("issue");
		ApplicationUser user = authenticationContext.getUser();
		SubTaskBean subTaskBean = getSubTaskBean(issue);
		VelocityRequestContext requestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();
		String selectedIssueId = requestContext.getRequestParameter("selectedIssueId");
		paramsBuilder.add("hasSubTasks", Boolean.valueOf(!subTaskBean.getSubTasks(getSubTaskView()).isEmpty()));
		paramsBuilder.add("selectedIssueId", selectedIssueId);
		paramsBuilder.add("subTaskTable", new SubTaskTableRenderer(issue, user));
		return paramsBuilder.toMap();
	}

	private SubTaskBean getSubTaskBean(Issue issue) {
		HttpServletRequest request = getRequest();
		if (request != null) {
			SubTaskBean subtaskBean = (SubTaskBean) request.getAttribute((new StringBuilder()).append("atl.jira.subtask.bean.")
					.append(issue.getKey()).toString());
			if (subtaskBean != null) {
				return subtaskBean;
			} else {
				subtaskBean = subTaskManager.getSubTaskBean(issue, authenticationContext.getUser());
				request.setAttribute((new StringBuilder()).append("atl.jira.subtask.bean.").append(issue.getKey()).toString(),
						subtaskBean);
				return subtaskBean;
			}
		} else {
			return subTaskManager.getSubTaskBean(issue, authenticationContext.getUser());
		}
	}

    protected HttpServletRequest getRequest() {
        return ExecutingHttpRequest.get();
    }

	private String getSubTaskView() {
		VelocityRequestContext requestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();
		VelocityRequestSession session = requestContext.getSession();

		String requestParameter = requestContext.getRequestParameter("subTaskView");
		if (StringUtils.isNotBlank(requestParameter)) {
			if (requestParameter.equals("all")) {
				session.removeAttribute("jira.user.subtaskview");
				return "all";
			}

			session.setAttribute("jira.user.subtaskview", requestParameter);
			return requestParameter;
		}

		String subTaskView = (String) session.getAttribute("jira.user.subtaskview");
		return StringUtils.isNotBlank(subTaskView) ? subTaskView : "all";
	}

	private String getTableHtml(Issue issue, ApplicationUser user) {
		AggregateTimeTrackingBean aggregateTTBean = getAggregates(issue);
		SubTaskBean subTaskBean = getSubTaskBean(issue);
		String subTaskView = getSubTaskView();
		Collection<SubTask> issues = subTaskBean.getSubTasks(subTaskView);
		List<Issue> issueObjects = Lists.newArrayList();
		boolean atLeastOneIssueHasTimeTrackingData = false;

		for (SubTask subTask : issues) {
			Issue subTaskIssue = subTask.getSubTask();
			atLeastOneIssueHasTimeTrackingData = atLeastOneIssueHasTimeTrackingData || IssueUtils.hasTimeTracking(subTaskIssue);
			issueObjects.add(subTaskIssue);
		}

		IssueTableWebComponent issueTable = new IssueTableWebComponent();
		IssueTableLayoutBean layout;
		try {
			layout = tableLayoutFactory.getSubTaskIssuesLayout(user, issue, subTaskBean, subTaskView,
					atLeastOneIssueHasTimeTrackingData);
		} catch (ColumnLayoutStorageException e) {
			throw new RuntimeException(e);
		} catch (FieldException e) {
			throw new RuntimeException(e);
		}

		List<ColumnLayoutItem> columns = layout.getColumns();
		ColumnLayoutItem systemProgressColumn = getProgressColumn(columns);
		if (systemProgressColumn != null) {
			if (gpManager.hasPermission(IGlobalSettingsManager.SCN_TIMETRACKING, user)) {
				ScnProgressBarField scnProgressField = new ScnProgressBarField(templatingEngine, appProperties,
						authenticationContext, ttGraphBeanFactory, eiStore);
				ColumnLayoutItem scnProgressColumn = new ColumnLayoutItemImpl(scnProgressField,
						systemProgressColumn.getPosition());
				columns.add(columns.indexOf(systemProgressColumn) + 1, scnProgressColumn);
				layout.addCellDisplayParam("scnAggTTBean", getScnAggregates(issue));
			}
			if (!psManager.hasPermissionToViewWL(user, issue.getProjectObject())) {
				columns.remove(systemProgressColumn);
			}
		}

		layout.addCellDisplayParam("aggTTBean", aggregateTTBean);
		return issueTable.getHtml(layout, issueObjects, null);
	}

	private AggregateTimeTrackingBean getScnAggregates(Issue issue) {
		HttpServletRequest request = getRequest();
		if (request != null) {
			AggregateTimeTrackingBean aggregates = (AggregateTimeTrackingBean) request
					.getAttribute("scn.worklog.plugin.timetracking.aggregate.bean." + issue.getId());
			if (aggregates == null) {
				aggregates = getAggregateTimeTrackingBean(getExtendedIssue(issue));
				request.setAttribute("scn.worklog.plugin.timetracking.aggregate.bean." + issue.getId(), aggregates);
			}
			return aggregates;
		} else {
			return getAggregateTimeTrackingBean(getExtendedIssue(issue));
		}
	}

	private ColumnLayoutItem getProgressColumn(List<ColumnLayoutItem> columns) {
		for (ColumnLayoutItem column : columns) {
			if (column.getNavigableField() instanceof ProgressBarSystemField) return column;
		}

		return null;
	}

	private AggregateTimeTrackingBean getAggregates(Issue issue) {
		HttpServletRequest request = getRequest();
		if (request != null) {
			AggregateTimeTrackingBean aggregates = (AggregateTimeTrackingBean) request.getAttribute((new StringBuilder())
					.append("atl.jira.timetracking.aggregate.bean.").append(issue.getId()).toString());
			if (aggregates == null) {
				AggregateTimeTrackingCalculator calculator = aggregateTimeTrackingCalculatorFactory.getCalculator(issue);
				aggregates = calculator.getAggregates(issue);
				request.setAttribute((new StringBuilder()).append("atl.jira.timetracking.aggregate.bean.").append(issue.getId())
						.toString(), aggregates);
			}
			return aggregates;
		} else {
			AggregateTimeTrackingCalculator calculator = aggregateTimeTrackingCalculatorFactory.getCalculator(issue);
			return calculator.getAggregates(issue);
		}
	}

	private AggregateTimeTrackingBean getAggregateTimeTrackingBean(IScnExtendedIssue extIssue) {
		Assertions.notNull("extended issue", extIssue);
		Assertions.notNull("issue", extIssue.getIssue());

		AggregateTimeTrackingBean bean = new AggregateTimeTrackingBean(extIssue.getOriginalEstimate(), extIssue.getEstimate(),
				extIssue.getTimeSpent(), 0);
		if (extIssue.getIssue().isSubTask()) return bean;

		Collection<Issue> subTasks = extIssue.getIssue().getSubTaskObjects();
		if (subTasks == null || subTasks.isEmpty()) return bean;

		int subTaskCount = 0;
		for (Issue subTask : subTasks) {
			if (permissionManager.hasPermission(Permissions.BROWSE, subTask, authenticationContext.getUser())) {
				IScnExtendedIssue extSubTask = getExtendedIssue(subTask);

				bean.setRemainingEstimate(addAndPreserveNull(extSubTask.getEstimate(), bean.getRemainingEstimate()));
				bean.setOriginalEstimate(addAndPreserveNull(extSubTask.getOriginalEstimate(), bean.getOriginalEstimate()));
				bean.setTimeSpent(addAndPreserveNull(extSubTask.getTimeSpent(), bean.getTimeSpent()));
				bean.bumpGreatestSubTaskEstimate(extSubTask.getOriginalEstimate(), extSubTask.getEstimate(),
						extSubTask.getTimeSpent());

				subTaskCount++;
			}
		}
		bean.setSubTaskCount(subTaskCount);

		return bean;
	}

	private IScnExtendedIssue getExtendedIssue(Issue issue) {
		IScnExtendedIssue extIssue = eiStore.getByIssue(issue);

		if (extIssue == null) {
			extIssue = new ScnExtendedIssue(issue, null, null, null, null);
		}

		return extIssue;
	}

	public class SubTaskTableRenderer {

		private final Issue issue;
		private final ApplicationUser user;

		public String getHtml() {
			return ScnSubTasksContextProvider.this.getTableHtml(this.issue, this.user);
		}

		@Inject
		public SubTaskTableRenderer(Issue issue, ApplicationUser user) {
			// super();
			this.user = user;
			this.issue = issue;
		}
	}

}
