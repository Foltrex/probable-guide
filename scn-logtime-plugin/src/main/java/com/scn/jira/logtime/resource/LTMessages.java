package com.scn.jira.logtime.resource;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.jcip.annotations.Immutable;

@Immutable
@XmlRootElement
public class LTMessages {
	@XmlElement
	private String message;

	@XmlElement
	private boolean copied;

	@XmlElement
	private boolean typeChanged;

	@XmlElement
	private String wlId;

	@XmlElement
	private String wlIdExt;

	@XmlElement
	private ArrayList<String> issues;

	@XmlElement
	private ArrayList<Long> issueIds;

	public LTMessages(String message) {
		super();
		this.message = message;
	}

	public LTMessages(String message, boolean copied) {
		super();
		this.message = message;
		this.copied = copied;
	}

	public LTMessages(String message, boolean copied, boolean typeChanged) {
		super();
		this.message = message;
		this.copied = copied;
		this.typeChanged = typeChanged;
	}

	public LTMessages(String message, boolean copied, boolean typeChanged, String wlId) {
		super();
		this.wlId = wlId;
		this.message = message;
		this.copied = copied;
		this.typeChanged = typeChanged;
	}

	public LTMessages(String message, boolean copied, boolean typeChanged, String wlId, String wlIdExt) {
		super();
		this.wlId = wlId;
		this.wlIdExt = wlIdExt;
		this.message = message;
		this.copied = copied;
		this.typeChanged = typeChanged;
	}

	public LTMessages(ArrayList<String> issues, ArrayList<Long> issueIds) {
		super();
		this.issues = issues;
		this.issueIds = issueIds;
	}

	public void setWlId(String wlId) {
		this.wlId = wlId;
	}

	public LTMessages(ArrayList<String> issues) {
		super();
		this.issues = issues;
	}

	public ArrayList<String> getIssues() {
		return issues;
	}

	public void setIssues(ArrayList<String> issues) {
		this.issues = issues;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isCopied() {
		return copied;
	}

	public void setCopied(boolean copied) {
		this.copied = copied;
	}

	public boolean isTypeChanged() {
		return typeChanged;
	}

	public void setTypeChanged(boolean typeChanged) {
		this.typeChanged = typeChanged;
	}

	public ArrayList<Long> getIssueIds() {
		return issueIds;
	}

	public void setIssueIds(ArrayList<Long> issueIds) {
		this.issueIds = issueIds;
	}

	public String getWlIdExt() {
		return wlIdExt;
	}

	public void setWlIdExt(String wlIdExt) {
		this.wlIdExt = wlIdExt;
	}

	public String getWlId() {
		return wlId;
	}

}
