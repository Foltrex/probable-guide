package com.scn.jira.worklog.remote.service;

import java.rmi.RemoteException;

import com.atlassian.crowd.embedded.api.User;
import com.scn.jira.worklog.remote.service.object.RemoteScnWorklog;

/**
 * Created by IntelliJ IDEA.
 * User: Khadarovich
 * Date: 08.09.2010
 * Time: 17:55:52
 * To change this template use File | Settings | File Templates.
 */
public interface IRemoteScnWorklogService {
    RemoteScnWorklog[] getScnWorklogs(User user, String issueKey) throws RemoteException;//, RemoteValidationException;
}