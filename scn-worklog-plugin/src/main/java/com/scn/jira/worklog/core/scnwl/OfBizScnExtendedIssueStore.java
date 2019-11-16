package com.scn.jira.worklog.core.scnwl;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilDateTime;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExportAsService({ OfBizScnExtendedIssueStore.class })
@Named("ofBizScnExtendedIssueStore")
public class OfBizScnExtendedIssueStore implements IScnExtendedIssueStore {
	private final OfBizDelegator ofBizDelegator;
	public static final String ENTITY_EXTENDED_ISSUE = "ScnExtendedIssue";
	public static final String ENTITY_ISSUE = "Issue";

	@Inject
	public OfBizScnExtendedIssueStore(OfBizDelegator ofBizDelegator) {
		this.ofBizDelegator = ofBizDelegator;
	}

	public IScnExtendedIssue create(IScnExtendedIssue extIssue) throws DataAccessException {
		final GenericValue extIssueGV = ofBizDelegator.createValue(ENTITY_EXTENDED_ISSUE, createParamMap(extIssue));
		return convertToScnExtendedIssue(extIssue.getIssue(), extIssueGV);
	}

	public IScnExtendedIssue update(IScnExtendedIssue extIssue) throws DataAccessException {
		final GenericValue extIssueGV = ofBizDelegator.findByPrimaryKey(ENTITY_EXTENDED_ISSUE, extIssue.getId()),
				issueGV = extIssue.getIssue().getGenericValue();
		if (extIssueGV == null) {
			throw new DataAccessException("Could not find original scnExtendedIssue entity to update.");
		}
		extIssueGV.setFields(createParamMap(extIssue));
		issueGV.set("updated", UtilDateTime.nowTimestamp());
		try {
			extIssueGV.store();
			issueGV.store();
			return convertToScnExtendedIssue(extIssue.getIssue(), extIssueGV);
		} catch (GenericEntityException e) {
			throw new DataAccessException("Error occured while updating a scnExtendedIssue.", e);
		}
	}

	public boolean delete(Long id) throws DataAccessException {
		if (id == null) {
			throw new DataAccessException("Cannot remove a scnExtendedIssue with id null");
		}
		int numRemoved = ofBizDelegator.removeByAnd(ENTITY_EXTENDED_ISSUE, MapBuilder.build("id", id));
		return numRemoved == 1;
	}

	public IScnExtendedIssue getByIssue(Issue issue) throws DataAccessException {
		if (issue == null) {
			throw new IllegalArgumentException("Cannot find a scnExternalIssue by a null issue.");
		}
		final List<GenericValue> list = ofBizDelegator.findByAnd(ENTITY_EXTENDED_ISSUE,
				MapBuilder.build("issue", issue.getId()));
		if (list != null && !list.isEmpty()) {
			return convertToScnExtendedIssue(issue, list.get(0));
		} else {
			return null;
		}
	}

	protected Map<String, Object> createParamMap(IScnExtendedIssue extIssue) {
		if (extIssue == null) {
			throw new IllegalArgumentException("Cannot store a null scnExternalIssue.");
		} else if (extIssue.getIssue() == null) {
			throw new IllegalArgumentException("Cannot store a scnExternalIssue against a null issue.");
		} else {

			Map<String, Object> fields = new HashMap<String, Object>();
			fields.put("issue", extIssue.getIssue().getId());
			fields.put("timeoriginalestimate", extIssue.getOriginalEstimate());
			fields.put("timeestimate", extIssue.getEstimate());
			fields.put("timespent", extIssue.getTimeSpent());
			return fields;
		}
	}

	protected IScnExtendedIssue convertToScnExtendedIssue(Issue issue, GenericValue gv) {
		if (issue == null) {
			throw new IllegalArgumentException("Cannot create a scnExternalIssue instance against a null issue.");
		}
		if (gv == null) {
			throw new IllegalArgumentException("Cannot create a scnExternalIssue using a null genericValue.");
		}
		IScnExtendedIssue extIssue = new ScnExtendedIssue(issue, gv.getLong("id"), gv.getLong("timeoriginalestimate"),
				gv.getLong("timeestimate"), gv.getLong("timespent"));
		return extIssue;
	}
}
