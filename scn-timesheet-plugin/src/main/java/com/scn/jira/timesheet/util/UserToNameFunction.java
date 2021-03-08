package com.scn.jira.timesheet.util;

import com.atlassian.jira.user.ApplicationUser;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import java.util.Collection;

public class UserToNameFunction implements Function<ApplicationUser, String> {
    public static final UserToNameFunction INSTANCE = new UserToNameFunction();

    private UserToNameFunction() {
    }

    public static Collection<String> transform(Collection<ApplicationUser> users) {
        return Collections2.transform(users, INSTANCE);
    }

    public String apply(ApplicationUser from) {
        return from.getName();
    }
}
