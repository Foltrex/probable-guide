package com.scn.jira.logtime.representation;


public class CustomIssueDto {

	private final Long id;
	private final String summary;
	private final String key;

	public CustomIssueDto(Long id, String summary, String key) {
		this.id = id;
		this.summary = summary;
		this.key = key;
	}

	public Long getId() {
		return id;
	}

	public String getSummary() {
		return summary;
	}

	public String getKey() {
		return key;
	}
}
