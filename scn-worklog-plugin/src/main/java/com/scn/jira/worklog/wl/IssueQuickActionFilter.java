package com.scn.jira.worklog.wl;

import static com.atlassian.jira.issue.IssueFieldConstants.TIMETRACKING;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;

public class IssueQuickActionFilter extends BaseFilter {
	// private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IssueQuickActionFilter.class);

	@Override
	protected void overrideRequest(ScnRequestWrapper request, ScnResponseWrapper response) {
		 Map<String, String[]> requestParams = request.getParameterMap();
		
		 if (shouldOverrideRequest(requestParams))
		 {
		 requestParams = overrideParams(requestParams);
		 request.setParameterMap(requestParams);
		 }
	}

	@Override
	protected void overrideResponse(ScnRequestWrapper request, ScnResponseWrapper response) throws IOException {
		String content = response.getContent();

		if (shouldOverrideResponse(request.getParameterMap(), content)) {
			content = overrideJSON(content);
		}

		response.writeResponse(content);
	}

	protected boolean shouldOverrideResponse(Map<String, String[]> params, String content) {
		if (StringUtils.isBlank(content))
			return false;

		Project project = getProject(params);
		if (project == null)
			project = getProjectFromContent(content);

		if (project == null)
			return false;

		ApplicationUser user = authenticationContext.getUser();
		return !psManager.hasPermissionToViewWL(user, project);
	}

	private String overrideJSON(String content) throws IOException {
		try {
			JSONObject json = new JSONObject(content);

			if (json.has(FIELDS)) {
				JSONArray fields = json.getJSONArray(FIELDS);

				Map<String, JSONObject> fieldsMap = mapByKey(fields, ID);
				fieldsMap.remove(TIMETRACKING);

				json.put(FIELDS, fieldsMap.values());
			}

			if (json.has(USER_PREFERENCES)) {
				JSONObject userPreferences = json.getJSONObject(USER_PREFERENCES);
				JSONArray fields = userPreferences.getJSONArray(FIELDS);

				List<String> fieldsList = list(fields);
				fieldsList.remove(TIMETRACKING);

				userPreferences.put(FIELDS, fieldsList);
				json.put(USER_PREFERENCES, userPreferences);
			}

			if (json.has(SORTED_TABS)) {
				JSONArray tabs = json.getJSONArray(SORTED_TABS);

				List<JSONObject> tabsList = list(tabs);
				for (JSONObject tab : tabsList) {
					JSONArray fields = tab.getJSONArray(FIELDS);

					Map<String, JSONObject> fieldsMap = mapByKey(fields, ID);
					fieldsMap.remove(TIMETRACKING);

					tab.put(FIELDS, fieldsMap.values());
				}

				json.put(SORTED_TABS, tabsList);
			}

			return json.toString();
		} catch (JSONException e) {
			throw new IOException(e);
		}
	}

	private Project getProjectFromContent(String jsonContent) {
		Project project = null;

		try {
			JSONObject json = new JSONObject(jsonContent);
			JSONArray fields = json.getJSONArray(FIELDS);
			Map<String, JSONObject> fieldsMap = mapByKey(fields, ID);
			String editHtml = fieldsMap.get(PROJECT).getString(EDIT_HTML);

			Document doc = Jsoup.parse(editHtml);
			String pid = doc.select("input[id=project]").attr("value");
			project = projectManager.getProjectObj(Long.valueOf(pid));
		} catch (JSONException e) {
			return null;
		}

		return project;
	}

	private Map<String, JSONObject> mapByKey(JSONArray array, String keyId) {
		Map<String, JSONObject> objectsMap = new LinkedHashMap<String, JSONObject>();

		for (int i = 0; i < array.length(); i++) {
			JSONObject field = array.optJSONObject(i);
			objectsMap.put(field.optString(keyId), field);
		}

		return objectsMap;
	}

	private <T> List<T> list(JSONArray array) {
		List<T> stringList = new ArrayList<T>();

		for (int i = 0; i < array.length(); i++) {
			stringList.add((T) array.opt(i));
		}

		return stringList;
	}
}
