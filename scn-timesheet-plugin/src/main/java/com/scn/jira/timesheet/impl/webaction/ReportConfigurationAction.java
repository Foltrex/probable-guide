package com.scn.jira.timesheet.impl.webaction;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.web.action.browser.ReportConfiguredEvent;
import com.atlassian.jira.plugin.report.Report;
import com.atlassian.jira.plugin.report.ReportModuleDescriptor;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.seraph.util.RedirectUtils;
import com.google.common.collect.ImmutableMap;
import com.scn.jira.timesheet.report.timesheet.GroupByFieldValuesGenerator;
import com.scn.jira.timesheet.report.timesheet.PrioritiesValuesGenerator;
import com.scn.jira.timesheet.report.timesheet.ProjectValuesGenerator;
import com.scn.jira.timesheet.util.OptionalSearchRequestValuesGenerator;
import org.apache.commons.lang.StringUtils;
import webwork.action.ActionContext;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportConfigurationAction extends ProjectActionSupport {
    private final GroupByFieldValuesGenerator groupByFieldValuesGenerator = new GroupByFieldValuesGenerator();
    private final OptionalSearchRequestValuesGenerator optionalSearchRequestValuesGenerator = new OptionalSearchRequestValuesGenerator();
    private final PrioritiesValuesGenerator prioritiesValuesGenerator = new PrioritiesValuesGenerator();
    private final ProjectValuesGenerator projectValuesGenerator = new ProjectValuesGenerator();
    private final PluginAccessor pluginAccessor;
    private final EventPublisher eventPublisher;
    private Map<String, Object> parameters;
    private ReportModuleDescriptor descriptor;
    private String reportKey;
    private Report report;

    public ReportConfigurationAction(ProjectManager projectManager, PermissionManager permissionManager,
                                     PluginAccessor pluginAccessor, EventPublisher eventPublisher) {
        super(projectManager, permissionManager);
        this.pluginAccessor = pluginAccessor;
        this.eventPublisher = eventPublisher;
    }

    public String doExcelView() throws Exception {
        return this.forwardToInternalConfigureReport();
    }

    @Override
    protected void doValidation() {
        this.parameters = this.makeReportParams();
        this.getReportModule().validate(this, this.parameters);
    }

    @Override
    public String doDefault() throws Exception {
        this.parameters = this.makeReportParams();
        if (!this.validReport()) {
            return "input";
        } else {
            this.eventPublisher.publish(new ReportConfiguredEvent(this.getReportKey()));
            return super.doDefault();
        }
    }

    @Override
    protected String doExecute() throws Exception {
        if (!this.validReport()) {
            return "input";
        } else {
            this.parameters = this.makeReportParams();
            if (this.getReasons().contains(Reason.NOT_LOGGED_IN)) {
                return this.forceRedirect(RedirectUtils.getLoginUrl(this.getHttpRequest()));
            } else if (this.invalidInput()) {
                this.getErrorMessages().addAll(this.getErrors().values());
                return "input";
            } else {
                return this.forwardToInternalConfigureReport();
            }
        }
    }

    public String getParamValue(String key) {
        return (String) this.parameters.get(key);
    }

    public List getParamValues(String key) {
        Object values = this.parameters.get(key);
        if (values == null) {
            return Collections.emptyList();
        } else {
            return values instanceof String[] ? Arrays.asList((String[]) values) : Collections.singletonList(values.toString());
        }
    }

    public String getQueryString() {
        StringBuilder stringBuilder = new StringBuilder();
        boolean isFirstKey = true;
        Iterator var3 = this.parameters.keySet().iterator();

        while (true) {
            while (var3.hasNext()) {
                String key = (String) var3.next();
                Object value = this.parameters.get(key);
                if (value instanceof String) {
                    isFirstKey = this.appendUrlParameter(isFirstKey, key, (String) value, stringBuilder);
                } else if (value instanceof String[]) {
                    String[] var6 = (String[]) value;
                    int var7 = var6.length;

                    for (int var8 = 0; var8 < var7; ++var8) {
                        String s = var6[var8];
                        isFirstKey = this.appendUrlParameter(isFirstKey, key, s, stringBuilder);
                    }
                }
            }

            return stringBuilder.toString();
        }
    }

    private boolean appendUrlParameter(boolean firstKey, String key, String value, StringBuilder stringBuilder) {
        if (firstKey) {
            stringBuilder.append(this.encode(key)).append('=').append(this.encode(value));
        } else {
            stringBuilder.append('&').append(this.encode(key)).append('=').append(this.encode(value));
        }

        return false;
    }

    private String encode(String key) {
        return JiraUrlCodec.encode(key);
    }

    private String forwardToInternalConfigureReport() throws ServletException, IOException {
        String path = this.getHttpRequest().getServletPath().replaceFirst("(\\w+)(?=!?\\w*\\.)", "ConfigureReport");
        RequestDispatcher requestDispatcher = this.getHttpRequest().getRequestDispatcher(path);
        requestDispatcher.forward(this.getHttpRequest(), this.getHttpResponse());
        return "none";
    }

    private boolean validReport() {
        if (StringUtils.isEmpty(this.getReportKey())) {
            this.addErrorMessage(this.getText("report.configure.error.no.report.key"));
            return false;
        } else if (this.getReport() != null && this.getReportModule().showReport()) {
            return true;
        } else {
            this.addErrorMessage(this.getText("report.configure.error.no.report", this.getReportKey()));
            return false;
        }
    }

    private Map<String, Object> makeReportParams() {
        Map params = ActionContext.getParameters();
        Map<String, Object> reportParams = new LinkedHashMap<>(params.size());

        for (Object o : params.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            String key = (String) entry.getKey();
            if (((String[]) entry.getValue()).length == 1) {
                reportParams.put(key, ((String[]) entry.getValue())[0]);
            } else {
                reportParams.put(key, entry.getValue());
            }
        }

        return reportParams;
    }

    public Map<String, String> getProjects() {
        Map<String, String> result = projectValuesGenerator.getValues(ImmutableMap.of("User", this.getLoggedInUser()));
        return result;
    }

    public Map<String, String> getFilters() {
        return optionalSearchRequestValuesGenerator.getValues(ImmutableMap.of("User", this.getLoggedInUser()));
    }

    public Map<String, String> getPriorities() {
        return prioritiesValuesGenerator.getValues(Collections.emptyMap());
    }

    public Map<String, String> getGroupByFields() {
        return groupByFieldValuesGenerator.getValues(Collections.emptyMap());
    }

    public String getTargetUsersJson() throws JSONException {
        UserManager userManager = this.getUserManager();
        JSONArray result = new JSONArray();
        String targetUser = this.getHttpRequest().getParameter("targetUser");
        if (!StringUtils.isEmpty(targetUser)) {
            String[] targetUserNames = targetUser.split(",");
            for (String name : targetUserNames) {
                ApplicationUser user = userManager.getUserByName(name);
                if (user != null) {
                    result.put((new JSONObject()).put("name", name).put("displayName", user.getDisplayName()));
                }
            }
        }

        return result.toString();
    }

    public String getTargetGroupsJson() throws JSONException {
        return this.getGroupJson("targetGroup");
    }

    public String getExcludeTargetGroupsJson() throws JSONException {
        return this.getGroupJson("excludeTargetGroup");
    }

    public String getReportKey() {
        return this.reportKey;
    }

    public void setReportKey(String reportKey) {
        this.reportKey = reportKey;
    }

    public ReportModuleDescriptor getReport() {
        if (this.descriptor == null) {
            this.descriptor = (ReportModuleDescriptor) this.pluginAccessor.getEnabledPluginModule(this.reportKey);
        }

        return this.descriptor;
    }

    private String getGroupJson(String param) throws JSONException {
        JSONArray result = new JSONArray();
        List groups = this.getParamValues(param);

        JSONObject obj;
        for (Iterator var4 = groups.iterator(); var4.hasNext(); result.put(obj)) {
            Object key = var4.next();
            obj = (new JSONObject()).put("name", key);
            if ("@any".equals(key)) {
                obj.put("html", this.getI18nHelper().getText("timesheet.group.any"));
            }
        }

        return result.toString();
    }

    private Report getReportModule() {
        if (this.report == null) {
            this.report = this.getReport().getModule();
        }

        return this.report;
    }
}
