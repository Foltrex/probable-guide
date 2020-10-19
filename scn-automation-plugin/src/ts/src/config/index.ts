const Config = {
  baseURL() {
    return window["AJS"] ? window["AJS"].params.baseURL : "";
  },
  API: "rest/scn-automation/latest",
  JIRA_API: "rest/api/2",
  INVALID_CODE: 400,
  FORBIDDEN_CODE: 403,
  toInit: window["AJS"]
    ? window["AJS"].toInit
    : function (callback: () => void): void {
        callback();
      },
};

export default Config;
