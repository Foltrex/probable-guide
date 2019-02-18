package com.scn.jira.logtime.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.jira.user.util.UserManager;

import com.scn.jira.logtime.store.ExtWorklogLogtimeStore;
import com.scn.jira.logtime.store.IExtWorklogLogtimeStore;
import com.scn.jira.logtime.util.TextFormatUtil;
import com.scn.jira.worklog.core.scnwl.DefaultScnWorklogManager;
import com.scn.jira.worklog.core.scnwl.IScnWorklogManager;
import com.scn.jira.worklog.core.wl.DefaultExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.ExtendedWorklogManager;
import com.scn.jira.worklog.core.wl.ExtendedWorklogManagerImpl;
import com.scn.jira.worklog.scnwl.DefaultScnWorklogService;
import com.scn.jira.worklog.scnwl.IScnWorklogService;

/**
 * REST resource that provides a list of projects in JSON format.
 */
@Named
@Path("/getIssues")
public class LTIssueResource {
	private UserManager userManager;
	private PermissionManager permissionManager;
	private UserUtil userUtil;
	private JiraAuthenticationContext authenticationContext;
	private final OutlookDateManager outlookDateManager;

	private ProjectManager projectManager;
	private IssueManager issueManager;

	private ProjectRoleManager projectRoleManager;
	private WorklogManager worklogManager;
	private ExtendedConstantsManager extendedConstantsManager;
	private IScnWorklogManager scnWorklogManager;
	private ExtendedWorklogManager extendedWorklogManager;
	private IScnWorklogService scnDefaultWorklogService;

	/**
	 * Constructor.
	 * 
//	 * @param userManager
	 *            a SAL object used to find remote usernames in Atlassian
	 *            products
	 * @param userUtil
	 *            a JIRA object to resolve usernames to JIRA's internal
	 *            {@code com.opensymphony.os.User} objects
	 * @param permissionManager
	 *            the JIRA object which manages permissions for users and
	 *            projects
	 */

	@Inject
	public LTIssueResource(@ComponentImport PermissionManager permissionManager,
               @ComponentImport UserUtil userUtil, @ComponentImport JiraAuthenticationContext authenticationContext,
               @ComponentImport OutlookDateManager outlookDateManager, @ComponentImport ProjectManager projectManager,
               @ComponentImport IssueManager issueManager, @ComponentImport ProjectRoleManager projectRoleManager,
               @ComponentImport WorklogManager worklogManager,
               @ComponentImport DefaultExtendedConstantsManager defaultExtendedConstantsManager,
               @ComponentImport DefaultScnWorklogManager scnWorklogManager,
               @ComponentImport ExtendedWorklogManagerImpl extendedWorklogManager,
               @ComponentImport DefaultScnWorklogService scnDefaultWorklogService) {
		super();
		this.userManager = ComponentAccessor.getUserManager();
		this.permissionManager = permissionManager;
		this.userUtil = userUtil;
		this.authenticationContext = authenticationContext;
		this.outlookDateManager = outlookDateManager;
		this.projectManager = projectManager;
		this.issueManager = issueManager;
		this.projectRoleManager = projectRoleManager;
		this.worklogManager = worklogManager;
		this.extendedConstantsManager = defaultExtendedConstantsManager;
		this.scnWorklogManager = scnWorklogManager;
		this.extendedWorklogManager = extendedWorklogManager;
		this.scnDefaultWorklogService=scnDefaultWorklogService;

	}

	private CacheControl getNoCacheControl() {
		CacheControl noCache = new CacheControl();
		noCache.setNoCache(true);
		return noCache;
	}

	/**
	 * Returns the list of projects browsable by the user in the specified
	 * request.
	 * 
	 * @param request
	 *            the context-injected {@code HttpServletRequest}
	 * @return a {@code Response} with the marshalled projects
	 */
	@GET
	@AnonymousAllowed
	@Produces({ "application/json", "application/xml" })
	public Response getIssues(@Context HttpServletRequest request,
			@QueryParam("projectId") String projectId) {

		IExtWorklogLogtimeStore iExtWorklogLogtimeStore = new ExtWorklogLogtimeStore(
				userManager, projectManager, issueManager, permissionManager,
				projectRoleManager, worklogManager, extendedConstantsManager,
				extendedWorklogManager,scnDefaultWorklogService);

		Project prj = this.projectManager.getProjectObj(Long.valueOf(projectId
				.trim()));
		List<Issue> issues = iExtWorklogLogtimeStore.getIssuesByProjects(prj);

		Collections.sort(issues, new Comparator<Issue>() {
			
			public int compare(Issue o1, Issue o2) {			
				
				if (o1.getKey() != null && o2.getKey() != null && o1.getKey().indexOf("-")!=-1 && o2.getKey().indexOf("-")!=-1 ) {
					
					String name1 = matchesPatternName(o1.getKey());// o1.getKey().substring(0,o1.getKey().indexOf("-"));
					String name2 = matchesPatternName(o2.getKey());
					
					int key1 = matchesPatternKey(o1.getKey()); //o1.getKey().substring(o1.getKey().indexOf("-"),o1.getKey().length());
					int key2 = matchesPatternKey(o2.getKey());
					
					if(name1.equals(name2)){
						return key1>key2?1:-1;
					}else{
						return name1.compareTo(name2);
					}					
				}
				else {
					return 0;
				}
			}
		});

		ArrayList<String> issuesList = new ArrayList<String>();
		ArrayList<Long> issuesIds = new ArrayList<Long>();
		issuesList.add("");
		issuesIds.add(0L);
		for (Issue issue : issues) {
			issuesIds.add(issue.getId());
			issuesList.add(TextFormatUtil.replaceHTMLSymbols(issue.getKey()+" - "+issue.getSummary()));
		}
	

		LTMessages message = new LTMessages(issuesList, issuesIds);

		return Response.ok(message).build();
	}

	public static String matchesPatternName(String issueKey) {
		
		java.util.regex.Pattern p1 = java.util.regex.Pattern.compile("^(\\w+)-(\\d+)$");
		Matcher m1 = p1.matcher(issueKey);
		try{
			if (m1.find()) {
				return m1.group(1);
			}			
		}catch(NumberFormatException e){			
			return issueKey;
		}
		return issueKey;
	}
	
	public static int matchesPatternKey(String issueKey) {
		
		java.util.regex.Pattern p1 = java.util.regex.Pattern.compile("^(\\w+)-(\\d+)$");
		Matcher m1 = p1.matcher(issueKey);
		try{
			if (m1.find()) {
				String key =m1.group(2);
				return Integer.parseInt(key);
			}
		}
		catch(NumberFormatException e){
			return 0;
		}
		return 0;
	}
		

}
