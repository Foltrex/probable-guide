const Config = {
  baseURL() {
    return window["AJS"] ? window["AJS"].params.baseURL : "";
  },
  API: "rest/scn-automation/latest",
  JIRA_API: "rest/api/2",
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
};

export default Config;
