package com.scn.jira.logtime.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueKey;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.user.util.UserManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.scn.jira.logtime.representation.CustomIssueDto;
import com.scn.jira.logtime.store.ExtWorklogLogtimeStore;
import com.scn.jira.logtime.store.IExtWorklogLogtimeStore;
import com.scn.jira.logtime.store.IScnWorklogLogtimeStore;
import com.scn.jira.logtime.util.DateUtils;
import com.scn.jira.logtime.util.TextFormatUtil;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import com.scn.jira.worklog.core.scnwl.IScnWorklogManager;
import com.scn.jira.worklog.core.scnwl.IScnWorklogStore;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.core.settings.IScnUserBlockingManager;
import com.scn.jira.worklog.core.wl.ExtWorklog;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.ExtendedWorklogManager;
import com.scn.jira.worklog.core.wl.WorklogType;
import com.scn.jira.worklog.scnwl.IScnWorklogService;
import com.scn.jira.logtime.representation.LTIssueRepresentation;
import com.scn.jira.logtime.representation.LTProjectRepresentation;
import com.scn.jira.logtime.representation.WLLineIssueRepresentation;
import com.scn.jira.logtime.representation.WLRepresentation;
import com.scn.jira.logtime.representation.WLsRepresentation;
import com.scn.jira.logtime.representation.WLsTypeRepresentation;
import com.scn.jira.logtime.store.ScnWorklogLogtimeStore;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.EntityFindOptions;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;

public class WorklogLogtimeManager implements IWorklogLogtimeManager {

	private UserManager userManager;
	private ProjectManager projectManager;
	private IssueManager issueManager;
	private PermissionManager permissionManager;
	private ProjectRoleManager projectRoleManager;
	private WorklogManager worklogManager;
	private ExtendedConstantsManager extendedConstantsManager;
	private ExtendedWorklogManager extendedWorklogManager;
	
	private UserUtil userUtil;
	private IScnWorklogManager scnWorklogManager;
	private IScnWorklogStore ofBizScnWorklogStore;
	private IScnProjectSettingsManager projectSettignsManager;
	private IScnUserBlockingManager scnUserBlockingManager;
	private IExtWorklogLogtimeStore iExtWorklogLogtimeStore;
	private IScnWorklogLogtimeStore iScnWorklogLogtimeStore;
	private IScnWorklogService scnDefaultWorklogService;
	
	private Map<String, Map<String, Integer>> calendarMap;
	
	public WorklogLogtimeManager(UserManager userManager, ProjectManager projectManager, IssueManager issueManager, UserUtil userUtil,
			PermissionManager permissionManager, IScnWorklogManager scnWorklogManager, ProjectRoleManager projectRoleManager,
			WorklogManager worklogManager, ExtendedConstantsManager extendedConstantsManager, IScnWorklogStore ofBizScnWorklogStore,
			IScnProjectSettingsManager projectSettignsManager, IScnUserBlockingManager scnUserBlockingManager,IScnWorklogService scnDefaultWorklogService) {
		this.userManager = userManager;
		this.projectManager = projectManager;
		this.issueManager = issueManager;
		this.userUtil = userUtil;
		this.permissionManager = permissionManager;
		this.scnWorklogManager = scnWorklogManager;
		this.projectRoleManager = projectRoleManager;
		this.worklogManager = worklogManager;
		this.extendedConstantsManager = extendedConstantsManager;
		this.ofBizScnWorklogStore = ofBizScnWorklogStore;
		this.projectSettignsManager = projectSettignsManager;
		this.scnUserBlockingManager = scnUserBlockingManager;
		this.iExtWorklogLogtimeStore = new ExtWorklogLogtimeStore(issueManager, worklogManager, extendedWorklogManager);
		this.iScnWorklogLogtimeStore = new ScnWorklogLogtimeStore(userManager, projectManager, issueManager, permissionManager, projectRoleManager,
				worklogManager, extendedConstantsManager, ofBizScnWorklogStore, projectSettignsManager, scnUserBlockingManager,scnDefaultWorklogService);
		this.calendarMap = new HashMap<String, Map<String, Integer>>();
	}
	
	public List<Issue> getIssuesByProjectBetweenDates(Project project) throws DataAccessException {
		return iExtWorklogLogtimeStore.getIssuesByProjects(project);
	}

	
	public List<IScnWorklog> getScnWorklogsByProjectUserBetweenDates(Project project, Date startDate, Date endDate, String user, boolean assignedCh)
			throws DataAccessException {
		return iScnWorklogLogtimeStore.getByProjectBetweenDates(assignedCh, project, startDate, endDate, user);
	}
	
	public List<ExtWorklog> getExtWorklogsByProjectBetweenDates(Project project, Date startDate, Date endDate, String user, boolean assignedCh)
			throws DataAccessException {
		return iExtWorklogLogtimeStore.getExtWorklogsByProjectBetweenDates(assignedCh, project, startDate, endDate, user);
	};

	private List<CustomIssueDto> getCustomIssues(Project project) {
		OfBizListIterator issueIterator = null;
		List<CustomIssueDto> issues = new ArrayList<CustomIssueDto>();
		try {
			issueIterator = ComponentAccessor.getOfBizDelegator().findListIteratorByCondition("Issue", new EntityFieldMap(ImmutableMap.of("project", project.getId()), EntityOperator.AND), (EntityCondition)null, ImmutableList.of("id", "number", "summary"), (List)null, (EntityFindOptions)null);
			for(GenericValue issueIdGV = issueIterator.next(); issueIdGV != null; issueIdGV = issueIterator.next()) {
				issues.add(new CustomIssueDto(issueIdGV.getLong("id"), IssueKey.format(project, issueIdGV.getLong("number")), issueIdGV.getString("summary")));
			}
		} finally {
			if (issueIterator != null) {
				issueIterator.close();
			}
		}
		return issues;
	}

	public LTProjectRepresentation getLTProjectRepresentationBetweenDates(ApplicationUser loggedUser, Project project, Date startDate, Date endDate,
			boolean scnWlCheck, boolean extWlCheck, boolean assignedCh, String user) throws DataAccessException {
		
		Date startPlus = startDate;
		Date endPlus = endDate;
		//if(user.equals(loggedUser.getName())){
			startPlus = DateUtils.getStartDate(-28, startDate);
			endPlus = DateUtils.getEndDate(28, endDate);
		//}
		Map<String, Map<String, Integer>> calMap = getCalendarMap();
		Map<String, Integer> userCalMap = null;
		if(calMap!=null){
			userCalMap = calMap.get(user);
		}
		
		int multiplier = ((scnWlCheck && extWlCheck) ? 2 : 1);
		LTProjectRepresentation ltProjectRepresentation = new LTProjectRepresentation();
		
		List<IScnWorklog> iScnWorklogs = getScnWorklogsByProjectUserBetweenDates(project, startPlus, endPlus, user, assignedCh);
		
		Map<Long, Map<String, List<IScnWorklog>>> issueWorkLogsMap = new HashMap<Long, Map<String, List<IScnWorklog>>>();
		for (IScnWorklog iScnWorklog : iScnWorklogs) {
			
			Long issueId = iScnWorklog.getIssue().getId();
			Map<String, List<IScnWorklog>> scnWorkLogsMap = issueWorkLogsMap.get(issueId);
			if (scnWorkLogsMap == null) {
				scnWorkLogsMap = new HashMap<String, List<IScnWorklog>>();
			}
			String key = DateUtils.shortStringDate(iScnWorklog.getStartDate());
			
			List<IScnWorklog> iList = scnWorkLogsMap.get(key);
			if (iList == null) {
				iList = new ArrayList<IScnWorklog>();
			}
			iList.add(iScnWorklog);
			scnWorkLogsMap.put(key, iList);
			
			issueWorkLogsMap.put(issueId, scnWorkLogsMap);
		}
		// ApplicationUser appuser =
		// ComponentAccessor.getUserManager().getUserByName(user);
		// ApplicationUser appuser =
		// ComponentAccessor.getUserManager().getUserByName(userManager.getRemoteUsername(request));
		
		boolean projectPermission = projectSettignsManager.hasPermissionToViewWL(loggedUser, project);
		
		List<ExtWorklog> extWorklogs = new ArrayList<ExtWorklog>();
		if (projectPermission) {
			extWorklogs = getExtWorklogsByProjectBetweenDates(project, startPlus, endPlus, user, assignedCh);
		}
		
		if (iScnWorklogs.size() == 0 && extWorklogs.size() == 0) {
			return null;
		}
		Map<Long, Map<String, List<ExtWorklog>>> issueExtWorkLogsMap = new HashMap<Long, Map<String, List<ExtWorklog>>>();
		for (ExtWorklog extWorklog : extWorklogs) {
			Long issueId = extWorklog.getIssue().getId();
			Map<String, List<ExtWorklog>> extWorkLogsMap = issueExtWorkLogsMap.get(issueId);
			if (extWorkLogsMap == null) {
				extWorkLogsMap = new HashMap<String, List<ExtWorklog>>();
			}
			
			String key = DateUtils.shortStringDate(extWorklog.getStartDate());
			List<ExtWorklog> iList = extWorkLogsMap.get(key);
			if (iList == null) {
				iList = new ArrayList<ExtWorklog>();
			}
			iList.add(extWorklog);
			extWorkLogsMap.put(key, iList);
			
			issueExtWorkLogsMap.put(issueId, extWorkLogsMap);
		}
		
		System.out.println("User: " + user + "Worklogs for project: " + project.getName() + " Size Int : " + iScnWorklogs.size() + " Size Ext : "
				+ extWorklogs.size());
		
		ltProjectRepresentation.setId(project.getId());
		ltProjectRepresentation.setName(TextFormatUtil.replaceHTMLSymbols(project.getName()));
		ltProjectRepresentation.setKey(project.getKey());
		ltProjectRepresentation.setPermission(projectPermission);
		
		List<LTIssueRepresentation> issuesRepresentations = new ArrayList<LTIssueRepresentation>();
		
		ltProjectRepresentation.setIssues(issuesRepresentations);
		
		// А тут мы побежали по ишью у проекта в которых есть ворклоги хоть
		// какие за этот период у этого автора
		int projectSpanSize = 0;
		// For total counting
		Map<String, Integer> mapTotalScn = new HashMap<String, Integer>();
		Map<String, Integer> mapTotalExt = new HashMap<String, Integer>();
		List<String> dates = DateUtils.getDatesList(startDate, endDate);
		List<String> datesPlus = DateUtils.getDatesList(startPlus, endPlus);
		
		for (String date : datesPlus) {
			mapTotalScn.put(date, new Integer(0));
			mapTotalExt.put(date, new Integer(0));
		}
		List<CustomIssueDto> issues = getCustomIssues(project);

		Collections.sort(issues, new Comparator<CustomIssueDto>() {
			
			public int compare(CustomIssueDto o1, CustomIssueDto o2) {
				if (o1.getSummary() != null && o2.getSummary() != null) {
					return o1.getSummary().compareTo(o2.getSummary());
				}
				else {
					return 0;
				}
			}
		});
		
		for (CustomIssueDto issue : issues) {
			Set<WLLineIssueRepresentation> wlLLineIssueRepresentationList = new HashSet<WLLineIssueRepresentation>();
			
			int issueSpanSize = 0;
			Long issueKey = issue.getId();
			
			// тут мы получили все ворклоги у данного ишьюса внешние и
			// внутренние
			Map<String, List<IScnWorklog>> scnWorkLogsMap = (Map<String, List<IScnWorklog>>) issueWorkLogsMap.get(issueKey);
			
			Map<String, List<ExtWorklog>> extWorkLogsMap = (Map<String, List<ExtWorklog>>) issueExtWorkLogsMap.get(issueKey);
			
			if (scnWorkLogsMap == null && extWorkLogsMap == null) {
				continue;
			}
			
			LTIssueRepresentation ltIssueRepresentation = new LTIssueRepresentation();
			ltIssueRepresentation.setId(issueKey);
			ltIssueRepresentation.setKey(issue.getKey());
			ltIssueRepresentation.setName(TextFormatUtil.replaceHTMLSymbols(issue.getSummary()));
			
			ltIssueRepresentation.setUrlName(makeUrl(ltIssueRepresentation.getKey(), ltIssueRepresentation.getName()));
			
			WLLineIssueRepresentation wlLLineIssueRepresentation = new WLLineIssueRepresentation();
			wlLLineIssueRepresentation.setIssueId(issueKey);
			
			Map<String, Map<String, List<WLsRepresentation>>> typeDayRepresentation = new HashMap<String, Map<String, List<WLsRepresentation>>>();
			
			Map<String, Integer> typeSize = new HashMap<String, Integer>();
			
			for (String date : datesPlus) {
				String worklogKey = date;
				
				// получаем ишью за день
				List<IScnWorklog> iList = (scnWorkLogsMap != null) ? scnWorkLogsMap.get(worklogKey) : null;
				List<ExtWorklog> iExtList = (extWorkLogsMap != null) ? extWorkLogsMap.get(worklogKey) : null;
				
				// We will count total for the project
				int dayTotalScn = 0;
				if (iList != null) {
					for (IScnWorklog iScnWorklog : iList) {
						dayTotalScn += iScnWorklog.getTimeSpent();
					}
				}
				Integer dayTotalFromMapScn = mapTotalScn.get(date) == null ? new Integer(0) : mapTotalScn.get(date);
				dayTotalFromMapScn += dayTotalScn;
				mapTotalScn.put(date, dayTotalFromMapScn);
				
				int dayTotalExt = 0;
				if (iExtList != null) {
					for (ExtWorklog extWorklog : iExtList) {
						dayTotalExt += extWorklog.getTimeSpent();
					}
				}
				Integer dayTotalFromMapExt = mapTotalExt.get(date) == null ? new Integer(0) : mapTotalExt.get(date);
				dayTotalFromMapExt += dayTotalExt;
				mapTotalExt.put(date, dayTotalFromMapExt);
				
				
				Integer status = 0;
				if(userCalMap!=null){
					status = userCalMap.get(DateUtils.string1ToString2(date));
				}				
				
				List<WLsRepresentation> wRepresentations = getWLsRepresentation(iList, iExtList, date,status);
				
				
				// тут мы заполнили хэшмэп type - day - list of doubled worklogs
				for (WLsRepresentation wRepresentation : wRepresentations) {
					String type = wRepresentation.getWlTypeId();
					if (typeDayRepresentation.get(type) != null) {
						Map<String, List<WLsRepresentation>> typeRepresentation = typeDayRepresentation.get(type);
						List<WLsRepresentation> wlsRepr = null;
						if (typeRepresentation.get(date) != null) {
							wlsRepr = typeRepresentation.get(date);
						}
						else {
							wlsRepr = new ArrayList<WLsRepresentation>();
						}
						
						wlsRepr.add(wRepresentation);
						typeRepresentation.put(date, wlsRepr);
						Integer max = (typeSize.get(type) != null && typeSize.get(type).intValue() > wlsRepr.size()) ? typeSize.get(type)
								: new Integer(wlsRepr.size());
						typeSize.put(type, max);
						typeDayRepresentation.put(type, typeRepresentation);
					}
					else {
						Map<String, List<WLsRepresentation>> typeRepresentation = new HashMap<String, List<WLsRepresentation>>();
						List<WLsRepresentation> wlsRepr = new ArrayList<WLsRepresentation>();
						
						wlsRepr.add(wRepresentation);
						typeRepresentation.put(date, wlsRepr);
						Integer max = (typeSize.get(type) != null && typeSize.get(type).intValue() > wlsRepr.size()) ? typeSize.get(type)
								: new Integer(wlsRepr.size());
						typeSize.put(type, max);
						typeDayRepresentation.put(type, typeRepresentation);
					}
				}
				
			}
			
			// теперь нужно финально сделать для каждого ишью список строк
			// (WLsTypeRepresentation)
			
			List<WLsTypeRepresentation> wLsTypeRepresentationList = new ArrayList<WLsTypeRepresentation>();
			
			Iterator typeDayIterator = typeDayRepresentation.keySet().iterator();
			// А тут мы побежали по ишью у проекта в которых есть ворклоги хоть
			// какие за этот период у этого автора
			while (typeDayIterator.hasNext()) {
				String type = (String) typeDayIterator.next();
				
				int maxSize = typeSize.get(type) == null ? 0 : typeSize.get(type).intValue();
				for (int i = 0; i < maxSize; i++) {
					WLsTypeRepresentation wLsTypeRepresentation = new WLsTypeRepresentation();
					wLsTypeRepresentation.setWlTypeId(type);
					WorklogType wlType = extendedConstantsManager.getWorklogTypeObject(type);
					
					wLsTypeRepresentation.setWlTypeName(wlType != null ? wlType.getName() : "Undefined Type");
					
					List<WLsRepresentation> wlsRepresentation = new ArrayList<WLsRepresentation>();
					// получили список ишью по дням..нужно их распихать теперь в
					// строчки
					Map<String, List<WLsRepresentation>> dayRepr = typeDayRepresentation.get(type);
					Map<String, WLsRepresentation> dayWlRepresentation = new TreeMap<String, WLsRepresentation>();
					for (String date : dates) {
						// получили список за день
						Integer status = 0;
						if(userCalMap!=null){
							status = userCalMap.get(DateUtils.string1ToString2(date));
							//System.out.println("We got the status of user  date: " + date+ " status " + status);
						}	
						
						List<WLsRepresentation> wLsRepresentations = dayRepr.get(date);
						if (wLsRepresentations == null || wLsRepresentations.size() == 0) {
							wlsRepresentation.add(new WLsRepresentation(date, status));
							dayWlRepresentation.put(date, new WLsRepresentation(date,status));
						}
						else {
							int k = i + 1;
							if (wLsRepresentations.size() >= k) {
								wlsRepresentation.add(wLsRepresentations.get(i));
								dayWlRepresentation.put(date, wLsRepresentations.get(i));
							}
							else {
								wlsRepresentation.add(new WLsRepresentation(date,status));
								dayWlRepresentation.put(date, new WLsRepresentation(date,status));
							}
						}
					}
					
					wLsTypeRepresentation.setWlsRepresentation(wlsRepresentation);
					wLsTypeRepresentation.setWlsRepresentationMap(dayWlRepresentation);
					
					wLsTypeRepresentationList.add(wLsTypeRepresentation);
				}
				
			}
			
			issueSpanSize = issueSpanSize + wLsTypeRepresentationList.size();
			
			// у ищью посетили список его строчек
			wlLLineIssueRepresentation.setWlsRepresentation(wLsTypeRepresentationList);
			
			wlLLineIssueRepresentationList.add(wlLLineIssueRepresentation);
			
			// теперь ишью добавили все его строчки
			ltIssueRepresentation.setWlTypes(wlLLineIssueRepresentationList);
			int issueMultiplier = (multiplier==2 && !projectPermission)?1:multiplier;
			ltIssueRepresentation.setRowspan(issueSpanSize == 0 ? issueMultiplier : new Long(issueSpanSize * issueMultiplier));
			
			projectSpanSize = projectSpanSize + issueSpanSize;
			// теперь к списку всех ищью рпоекта добавили новую ишью
			issuesRepresentations.add(ltIssueRepresentation);
			
		}
		Map<String, Integer> totalScnList = new HashMap<String, Integer>();
		Map<String, Integer> totalExtList = new HashMap<String, Integer>();
		Integer totalProjectScn = 0;
		Integer totalProjectExt = 0;
		
		for (String date : dates) {
			totalScnList.put(date, mapTotalScn.get(date));
			totalExtList.put(date, mapTotalExt.get(date));
			totalProjectScn = totalProjectScn + mapTotalScn.get(date);
			totalProjectExt = totalProjectExt + mapTotalExt.get(date);
		}
		ltProjectRepresentation.setScnWlTotal(totalScnList);
		ltProjectRepresentation.setExtWlTotal(totalExtList);
		ltProjectRepresentation.setScnPrTotal(TextFormatUtil.timeToString(String.valueOf(totalProjectScn)));
		ltProjectRepresentation.setExtPrTotal(TextFormatUtil.timeToString(String.valueOf(totalProjectExt)));
		
		// посетили проекту список ишьюсов
		ltProjectRepresentation.setIssues(issuesRepresentations);
		
		int projectMultiplier = (multiplier==2 && !projectPermission)?1:multiplier;
		ltProjectRepresentation.setRowspan(new Long(projectSpanSize * projectMultiplier));
		
		return ltProjectRepresentation;
	};
	
	public List<WLsRepresentation> getWLsRepresentation(List<IScnWorklog> scnWorklogs, List<ExtWorklog> extWorklogs, String day, Integer status) {
		if (scnWorklogs == null) {
			scnWorklogs = new ArrayList<IScnWorklog>();
		}
		if (extWorklogs == null) {
			extWorklogs = new ArrayList<ExtWorklog>();
		}
		
		List<WLsRepresentation> wlList = new ArrayList<WLsRepresentation>();
		List<String> ids = new ArrayList<String>();
		for (IScnWorklog scnWorklog : scnWorklogs) {
			WLsRepresentation wlRepresentation = new WLsRepresentation();
			wlRepresentation.setDay(day);
	
			WorklogType wlType = extendedConstantsManager.getWorklogTypeObject(scnWorklog.getWorklogTypeId());
			
			wlRepresentation.setWlTypeId(wlType!=null?scnWorklog.getWorklogTypeId():"0");
			
			wlRepresentation.setWlTypeName(wlType != null ? wlType.getName() : "Undefined Type");
			
			wlRepresentation.setWlScnRepresentation(makeWLRepresentation(scnWorklog, status));
			if (scnWorklog.getLinkedWorklog() != null) {
				ids.add(String.valueOf(scnWorklog.getLinkedWorklog().getId()));
				ExtWorklog extWorklog = getExtWorklogById(extWorklogs, scnWorklog.getLinkedWorklog().getId());
				if (extWorklog != null) {
					wlRepresentation.setWlExtRepresentation(makeWLRepresentation(extWorklog, status));
				}
				else {
					wlRepresentation.setWlExtRepresentation(new WLRepresentation(day, status));
					
				}
			}
			else {
				wlRepresentation.setWlExtRepresentation(new WLRepresentation(day,status));
			}
			wlList.add(wlRepresentation);
		}
		for (ExtWorklog extWorklog : extWorklogs) {
			if (!ids.contains(String.valueOf(extWorklog.getId()))) {
				
				WLsRepresentation wlRepresentation = new WLsRepresentation();
				wlRepresentation.setDay(day);
				
				WorklogType wlType = extendedConstantsManager.getWorklogTypeObject(extWorklog.getWorklogTypeId());
				wlRepresentation.setWlTypeId(wlType!=null?extWorklog.getWorklogTypeId():"0");
				wlRepresentation.setWlTypeName(wlType != null ? wlType.getName() : "Undefined Type");
				
				wlRepresentation.setWlExtRepresentation(makeWLRepresentation(extWorklog, status));
				wlRepresentation.setWlScnRepresentation(new WLRepresentation(day,status));
				wlList.add(wlRepresentation);
			}
		}
		
		return wlList;
	}
	
	public WLRepresentation makeWLRepresentation(IScnWorklog scnWorklog,Integer status) {
		
		WLRepresentation wlRepresentation = new WLRepresentation(scnWorklog.getId(), (scnWorklog.getLinkedWorklog() != null ? scnWorklog
				.getLinkedWorklog().getId() : null), scnWorklog.getWorklogTypeId(), "", new Boolean(false));
		wlRepresentation.setTimeSpent(String.valueOf(scnWorklog.getTimeSpent()));
		wlRepresentation.setTimeSpentString(TextFormatUtil.timeToString(String.valueOf(scnWorklog.getTimeSpent())));
		wlRepresentation.setTimeSpentString2(TextFormatUtil.timeToString2(String.valueOf(scnWorklog.getTimeSpent())));
		wlRepresentation.setDate(DateUtils.fullStringDate(scnWorklog.getStartDate()));		 
		wlRepresentation.setDayColor(DateUtils.getDayColor(scnWorklog.getStartDate(), status));
		wlRepresentation.setComment(scnWorklog.getComment());
		
		return wlRepresentation;
	}
	
	public WLRepresentation makeWLRepresentation(ExtWorklog extWorklog, Integer status) {
		
		WLRepresentation wlRepresentation = new WLRepresentation(extWorklog.getId(), null, extWorklog.getWorklogTypeId(), "", new Boolean(true));
		wlRepresentation.setTimeSpent(String.valueOf(extWorklog.getTimeSpent()));
		wlRepresentation.setTimeSpentString(String.valueOf(extWorklog.getTimeSpent()));
		wlRepresentation.setTimeSpentString(TextFormatUtil.timeToString(String.valueOf(extWorklog.getTimeSpent())));
		wlRepresentation.setTimeSpentString2(TextFormatUtil.timeToString2(String.valueOf(extWorklog.getTimeSpent())));
		wlRepresentation.setDate(DateUtils.fullStringDate(extWorklog.getStartDate()));
		wlRepresentation.setDayColor(DateUtils.getDayColor(extWorklog.getStartDate(),status));
		wlRepresentation.setComment(extWorklog.getComment());
		
		return wlRepresentation;
	}
	
	public ExtWorklog getExtWorklogById(List<ExtWorklog> extWorklogs, Long id) throws DataAccessException {
		for (ExtWorklog extWorklog : extWorklogs) {
			if (extWorklog.getId().longValue() == id.longValue()) {
				return extWorklog;
			}
		}
		
		return null;
	}
	
	public String makeUrl(String key, String name) {
		String allUrl = key + " - " + name;
		String url = "";
		
		String finalString = "";
		String[] words = allUrl.split(" ");
		for (String word : words) {
			finalString = finalString.length() == 0 ? word : finalString + " " + word;
			if (finalString.length() > 40) {
				return url + "...";
			}
			else {
				url = finalString;
			}
		}
		return url;
		
	}

	public Map<String, Map<String, Integer>> getCalendarMap() {
		return calendarMap;
	}

	public void setCalendarMap(Map<String, Map<String, Integer>> calendarMap) {
		this.calendarMap = calendarMap;
	}

	
	
	
	
}