package com.scn.jira.worklog.blocking.rest;

import java.util.Date;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.scn.jira.worklog.core.settings.IScnUserBlockingManager;

//@Path("/block-user")
public class UserBlockingService {	
	private static final Logger log = LoggerFactory.getLogger(UserBlockingService.class);
	
	private final UserUtil userUtil;
	private final IScnUserBlockingManager userBlockingManager;
	
	public UserBlockingService(
			UserUtil userUtil,
			IScnUserBlockingManager userBlockingManager) {
		this.userUtil = userUtil;
		this.userBlockingManager = userBlockingManager;
	}
	
	@PUT
	@Produces({MediaType.APPLICATION_JSON})
	public Response updateUserBlockingDate(
			@QueryParam("name") String name,
			@QueryParam("date") String dateString)
	{
		ApplicationUser user = userUtil.getUserByName(name);
		if (user == null) {
			log.warn("User " + name + " doesn't exist.");
			return Response.status(Status.BAD_REQUEST).entity("User " + name + " doesn't exist.").build();
		}
		
		Date date = userBlockingManager.parse(dateString);
		if (date == null && StringUtils.isBlank(dateString)) {
			log.warn("Date should match pattern " + userBlockingManager.getDatePattern() + " .");
			return Response.status(Status.BAD_REQUEST).entity("Date should match pattern " + userBlockingManager.getDatePattern() + " .").build();
		}

		userBlockingManager.setBlockingDate(user.getDirectoryUser(), date);
		log.info("User " + name + " was blocked until " + dateString + ".");
		return Response.status(Status.OK).entity("User " + name + " was blocked until " + dateString + ".").build();
	}
}
