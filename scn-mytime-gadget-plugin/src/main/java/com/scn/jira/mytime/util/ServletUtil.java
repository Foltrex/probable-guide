package com.scn.jira.mytime.util;

import javax.servlet.http.HttpServletRequest;

public final class ServletUtil {
	public static int getIntParam(HttpServletRequest request, String name, int defaultValue) {
		String param = request.getParameter(name);
		if ((param == null) || (param.length() == 0)) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(param);
		} catch (NumberFormatException ignored) {
		}
		return defaultValue;
	}
}
