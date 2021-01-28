package com.scn.jira.plugin.gadget.rest.user;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.user.search.UserSearchParams;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Named
@Path("/user-list")
public class UserResource {
    private final UserSearchService userSearchService;
    private static final Integer MAX_RESULTS = 5000;

    @Autowired
    public UserResource(UserSearchService userSearchService) {
        this.userSearchService = userSearchService;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getUsers() {
        ApplicationUser currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        Collection<ApplicationUser> userList = this.userSearchService
            .findUsers(new JiraServiceContextImpl(currentUser), "",
                new UserSearchParams.Builder()
                    .maxResults(MAX_RESULTS)
                    .allowEmptyQuery(true)
                    .includeActive(true)
                    .includeInactive(false).build());
        UserCollection userCollection = new UserCollection(convertUserToUserItem(currentUser),
            convertUserListToUserItems(userList));
        return Response.ok(userCollection).cacheControl(getNoCacheControl()).build();
    }

    private List<UserItem> convertUserListToUserItems(Collection<ApplicationUser> stdUserList) {
        List<UserItem> userList = new ArrayList<>();
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
        private UserItem currentUser;

        @XmlElement
        private List<UserItem> userList;

        public UserCollection(UserItem currentUser, List<UserItem> userList) {
            this.currentUser = currentUser;
            this.userList = userList;
        }

        public UserCollection() {
        }
    }
}
