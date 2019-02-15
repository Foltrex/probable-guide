package com.scn.jira.worklog.webfragment;

import com.atlassian.jira.bean.SubTaskBean;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.PluginParseException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Named
public class CustomSubTasksProgressContextProvider implements CacheableContextProvider {
    private final SubTaskManager subTaskManager;
    private final JiraAuthenticationContext authenticationContext;

    @Inject
    public CustomSubTasksProgressContextProvider(SubTaskManager subTaskManager, JiraAuthenticationContext authenticationContext) {
        this.subTaskManager = subTaskManager;
        this.authenticationContext = authenticationContext;
    }

    public void init(Map<String, String> params) throws PluginParseException {
    }

    public Map<String, Object> getContextMap(Map<String, Object> context) {
        MapBuilder<String, Object> paramsBuilder = MapBuilder.newBuilder(context);
        Issue issue = (Issue)context.get("issue");
        SubTaskBean subTaskBean = this.getSubTaskBean(issue, context);
        paramsBuilder.add("subTaskProgress", subTaskBean.getSubTaskProgress());
        return paramsBuilder.toMap();
    }

    public String getUniqueContextKey(Map<String, Object> context) {
        Issue issue = (Issue)context.get("issue");
        ApplicationUser user = (ApplicationUser)context.get("user");
        return issue.getId() + "/" + (user == null ? "" : user.getName());
    }

    private SubTaskBean getSubTaskBean(Issue issue, Map<String, Object> context) {
        HttpServletRequest request = this.getRequest(context);
        if (request != null) {
            SubTaskBean subtaskBean = (SubTaskBean)request.getAttribute("atl.jira.subtask.bean." + issue.getKey());
            if (subtaskBean != null) {
                return subtaskBean;
            } else {
                subtaskBean = this.subTaskManager.getSubTaskBean(issue, this.authenticationContext.getUser());
                request.setAttribute("atl.jira.subtask.bean." + issue.getKey(), subtaskBean);
                return subtaskBean;
            }
        } else {
            return this.subTaskManager.getSubTaskBean(issue, this.authenticationContext.getUser());
        }
    }

    protected HttpServletRequest getRequest(Map<String, Object> context) {
        return ExecutingHttpRequest.get();
    }
}
