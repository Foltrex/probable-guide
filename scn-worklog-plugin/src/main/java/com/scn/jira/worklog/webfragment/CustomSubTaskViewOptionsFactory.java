package com.scn.jira.worklog.webfragment;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.SimpleLinkFactory;
import com.atlassian.jira.plugin.webfragment.descriptors.SimpleLinkFactoryModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestSession;
import com.atlassian.plugin.web.api.WebItem;
import com.atlassian.plugin.web.api.model.WebFragmentBuilder;
import com.atlassian.plugin.web.api.provider.WebItemProvider;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@Named
public class CustomSubTaskViewOptionsFactory implements WebItemProvider, SimpleLinkFactory {
    private static final String ITEM_SECTION = "com.atlassian.jira.jira-view-issue-plugin:view-subtasks/drop/subtask-view-options";
    private final VelocityRequestContextFactory requestContextFactory;
    private final JiraAuthenticationContext authenticationContext;

    @Inject
    public CustomSubTaskViewOptionsFactory(VelocityRequestContextFactory requestContextFactory, JiraAuthenticationContext authenticationContext) {
        this.requestContextFactory = requestContextFactory;
        this.authenticationContext = authenticationContext;
    }

    public Iterable<WebItem> getItems(Map<String, Object> context) {
        VelocityRequestContext requestContext = this.requestContextFactory.getJiraVelocityRequestContext();
        I18nHelper i18n = this.authenticationContext.getI18nHelper();
        Issue issue = (Issue)context.get("issue");
        VelocityRequestSession session = requestContext.getSession();
        String baseUrl = requestContext.getBaseUrl();
        String subTaskView = (String)session.getAttribute("jira.user.subtaskview");
        boolean showingAll = "all".equals("all");
        if (StringUtils.isNotBlank(subTaskView)) {
            showingAll = subTaskView.equals("all");
        }

        WebItem allLink = (new WebFragmentBuilder(10)).id("subtasks-show-all").label(i18n.getText("viewissue.subtasks.tab.show.all.subtasks")).title(i18n.getText("viewissue.subtasks.tab.show.all.subtasks")).styleClass(showingAll ? "aui-list-checked aui-checked" : "aui-list-checked").webItem("com.atlassian.jira.jira-view-issue-plugin:view-subtasks/drop/subtask-view-options").url(baseUrl + "/browse/" + issue.getKey() + "?subTaskView=all#issuetable").build();
        WebItem openLink = (new WebFragmentBuilder(20)).id("subtasks-show-open").label(i18n.getText("viewissue.subtasks.tab.show.open.subtasks")).title(i18n.getText("viewissue.subtasks.tab.show.open.subtasks")).styleClass(!showingAll ? "aui-list-checked aui-checked" : "aui-list-checked").webItem("com.atlassian.jira.jira-view-issue-plugin:view-subtasks/drop/subtask-view-options").url(baseUrl + "/browse/" + issue.getKey() + "?subTaskView=unresolved#issuetable").build();
        String urlParams = showingAll ? "" : "&searchMode=unresolved";
        WebItem bulkOperationLink = (new WebFragmentBuilder(30)).id("subtasks-bulk-operation").label(i18n.getText("viewissue.subtasks.tab.bulk.operation")).title(i18n.getText("viewissue.subtasks.tab.bulk.operation")).styleClass("aui-list-checked").webItem("com.atlassian.jira.jira-view-issue-plugin:view-subtasks/drop/subtask-view-options").url(baseUrl + "/issue/bulkedit/BulkEdit1!default.jspa?reset=true&searchParent=" + issue.getKey() + urlParams).build();
        String jql = showingAll ? "parent=" + issue.getKey() : "parent=" + issue.getKey() + " AND resolution=Unresolved";
        WebItem openInIssueNavigator = (new WebFragmentBuilder(40)).id("subtasks-open-issue-navigator").label(i18n.getText("viewissue.subtasks.tab.open.issue.navigator")).title(i18n.getText("viewissue.subtasks.tab.open.issue.navigator")).styleClass("aui-list-checked").webItem("com.atlassian.jira.jira-view-issue-plugin:view-subtasks/drop/subtask-view-options").url(baseUrl + "/issues/?jql=" + this.encode(jql)).build();
        return CollectionBuilder.list(new WebItem[]{allLink, openLink, bulkOperationLink, openInIssueNavigator});
    }

    private String encode(String data) {
        try {
            return URLEncoder.encode(data, "UTF-8");
        } catch (UnsupportedEncodingException var3) {
            throw new RuntimeException(var3);
        }
    }

    @Override
    public void init(SimpleLinkFactoryModuleDescriptor simpleLinkFactoryModuleDescriptor) {

    }

    @Nonnull
    @Override
    public List<SimpleLink> getLinks(ApplicationUser applicationUser, Map<String, Object> map) {
        return null;
    }
}
