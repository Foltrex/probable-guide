package com.scn.jira.worklog.remote.service;

import java.rmi.RemoteException;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.scn.jira.worklog.remote.service.object.RemoteScnExtIssue;

/**
 * Created by IntelliJ IDEA.
 * User: Khadarovich
 * Date: 26.10.2010
 * Time: 18:36:42
 * To change this template use File | Settings | File Templates.
 */
public interface IRemoteScnExtIssueService {
    public RemoteScnExtIssue getScnExtIssue(User user, String issueKey) throws RemoteException;
    public RemoteScnExtIssue[] getScnExtIssues(User user, List<String> issueKeys) throws RemoteException;
}
