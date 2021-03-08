package com.scn.jira.timesheet.util;

import java.util.Comparator;

public class MyFullNameComparator implements Comparator<Object> {
	public int compare(Object o1, Object o2) {
		if ((o1 == null) && (o2 == null))
			return 0;
		if (o2 == null)
			return -1;
		if (o1 == null) {
			return 1;
		}
		MyUser u1 = (MyUser) o1;
		MyUser u2 = (MyUser) o2;

		String fullName1 = u1.getFullName();
		String fullName2 = u2.getFullName();

		if (fullName1 == null)
			return -1;
		if (fullName2 == null) {
			return 1;
		}
		return fullName1.toLowerCase().compareTo(fullName2.toLowerCase());
	}
}
