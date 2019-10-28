package com.scn.jira.plugin.gadget.rest.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

@Path("/user-list")
@AnonymousAllowed
public class UserResource {
	private final UserSearchService userSearchService;

	public UserResource() {
		this.userSearchService = ComponentAccessor.getUserSearchService();
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getUsers() {
		ApplicationUser currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
		Collection<ApplicationUser> userList = this.userSearchService
				.findUsersAllowEmptyQuery(new JiraServiceContextImpl(currentUser), null);
		UserCollection userCollection = new UserCollection(convertUserToUserItem(currentUser),
				convertUserListToUserItems(userList));
		return Response.ok(userCollection).cacheControl(getNoCacheControl()).build();
	}

	private List<UserItem> convertUserListToUserItems(Collection<ApplicationUser> stdUserList) {
		List<UserItem> userList = new ArrayList<UserResource.UserItem>();
		for (ApplicationUser u : stdUserList) {
			userList.add(convertUserToUserItem(u));
		}
		return userList;
	}

	private UserItem convertUserToUserItem(ApplicationUser stdUser) {
		return new UserResource.UserItem(stdUser.getName(), stdUser.getDisplayName());
	}

	private CacheControl getNoCacheControl() {
		CacheControl noCache = new CacheControl();
		noCache.setNoCache(true);
		return noCache;
	}

	@XmlRootElement
	public static class UserItem {

		@XmlElement
		private String value;

		@XmlElement
		private String label;

		UserItem(String value, String label) {
			this.value = value;
			this.label = label;
		}

		public UserItem() {
		}
	}

	@XmlRootElement
	public static class UserCollection {
		@XmlElement
		private UserResource.UserItem currentUser;

		@XmlElement
		private List<UserResource.UserItem> userList;

		public UserCollection(UserItem currentUser, List<UserItem> userList) {
			this.currentUser = currentUser;
			this.userList = userList;
		}

		public UserCollection() {
		}
	}
}
