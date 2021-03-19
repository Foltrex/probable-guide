package com.scn.jira.logtime.resource;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.scn.jira.logtime.store.IExtWorklogLogtimeStore;
import com.scn.jira.logtime.util.TextFormatUtil;
import lombok.RequiredArgsConstructor;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

@Named
@Path("/getIssues")
@RequiredArgsConstructor
public class LTIssueResource extends BaseResource {
    private final ProjectManager projectManager;
    private final PermissionManager permissionManager;
    private final IExtWorklogLogtimeStore iExtWorklogLogtimeStore;

    @GET
    @Produces({"application/json", "application/xml"})
    public Response getIssues(@Context HttpServletRequest request, @QueryParam("projectId") String projectId) {
        Project project = projectManager.getProjectObj(Long.valueOf(projectId.trim()));
        List<Issue> issues = iExtWorklogLogtimeStore.getIssuesByProjects(project);

        issues.sort((o1, o2) -> {
            if (o1.getKey() != null && o2.getKey() != null && o1.getKey().contains("-")
                && o2.getKey().contains("-")) {
                String name1 = matchesPatternName(o1.getKey());// o1.getKey().substring(0,o1.getKey().indexOf("-"));
                String name2 = matchesPatternName(o2.getKey());

                int key1 = matchesPatternKey(o1.getKey()); // o1.getKey().substring(o1.getKey().indexOf("-"),o1.getKey().length());
                int key2 = matchesPatternKey(o2.getKey());

                if (name1.equals(name2)) {
                    return key1 > key2 ? 1 : -1;
                } else {
                    return name1.compareTo(name2);
                }
            } else
                return 0;
        });

        ArrayList<String> issuesList = new ArrayList<>();
        ArrayList<Long> issuesIds = new ArrayList<>();
        issuesList.add("");
        issuesIds.add(0L);
        ApplicationUser user = getLoggedInUser();
        for (Issue issue : issues) {
            if (permissionManager.hasPermission(ProjectPermissions.BROWSE_PROJECTS, issue, user)) {
                issuesIds.add(issue.getId());
                issuesList.add(TextFormatUtil.replaceHTMLSymbols(issue.getKey() + " - " + issue.getSummary()));
            }
        }
        LTMessages message = new LTMessages(issuesList, issuesIds);

        return Response.ok(message).cacheControl(getNoCacheControl()).build();
    }

    private static String matchesPatternName(String issueKey) {
        java.util.regex.Pattern p1 = java.util.regex.Pattern.compile("^(\\w+)-(\\d+)$");
        Matcher m1 = p1.matcher(issueKey);
        try {
            if (m1.find()) {
                return m1.group(1);
            }
        } catch (NumberFormatException e) {
            return issueKey;
        }
        return issueKey;
    }

    private static int matchesPatternKey(String issueKey) {
        java.util.regex.Pattern p1 = java.util.regex.Pattern.compile("^(\\w+)-(\\d+)$");
        Matcher m1 = p1.matcher(issueKey);
        try {
            if (m1.find()) {
                String key = m1.group(2);
                return Integer.parseInt(key);
            }
        } catch (NumberFormatException e) {
            return 0;
        }
        return 0;
    }
}
