package com.scn.jira.plugin.gadget.rest.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

/**
 * Created by IntelliJ IDEA.
 * User: nsokolova
 * Date: 12/1/11
 * Time: 8:16 AM
 * To change this template use File | Settings | File Templates.
 */
@Path("/user-list")
@AnonymousAllowed
public class UserResource {

    private final UserManager userManager;

    public UserResource() {
        this.userManager = ComponentAccessor.getUserManager();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getUsers() {
        Collection<ApplicationUser> userList = this.userManager.getUsers();
        ApplicationUser currentUser = ComponentAccessor.getJiraAuthenticationContext().getUser();
        UserCollection userCollection = new UserCollection(convertUserToUserItem(currentUser)
                , convertUserListToUserItems(userList));
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

        private UserItem() {
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

        private UserCollection() {
        }
    }
}
