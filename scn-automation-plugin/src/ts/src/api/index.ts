import axios, { AxiosResponse } from "axios";
import { AutoTTDto } from "../models";

const getBaseURL = () => (window["AJS"] ? window["AJS"].params.baseURL : "");

export const getAllAutoTT = (): Promise<AxiosResponse> =>
  axios.get(`${getBaseURL()}/rest/scn-automation/latest/autotimetracking/user`);

export const getAutoTT = (id: number): Promise<AxiosResponse> =>
  axios.get(
    `${getBaseURL()}/rest/scn-automation/latest/autotimetracking/user/${id}`
  );

export const addAutoTT = (data: AutoTTDto): Promise<AxiosResponse> =>
  axios.post(
    `${getBaseURL()}/rest/scn-automation/latest/autotimetracking/user`,
    data
  );

export const updateAutoTT = (data: AutoTTDto): Promise<AxiosResponse> =>
  axios.put(
    `${getBaseURL()}/rest/scn-automation/latest/autotimetracking/user`,
    data
  );

export const removeAutoTT = (id: number): Promise<AxiosResponse> =>
  axios.delete(
    `${getBaseURL()}/rest/scn-automation/latest/autotimetracking/user/${id}`
  );

export const getUserAvatarURL = (key: string): string =>
  `${getBaseURL()}/secure/useravatar?ownerId=${key}`;

export const getUserProfileURL = (key: string): string =>
  `${getBaseURL()}/secure/ViewProfile.jspa?name=${key}`;

export const getProjectOrIssueURL = (key: string): string =>
  `${getBaseURL()}/browse/${key}`;

export const searchUsers = (query: string): Promise<AxiosResponse> =>
  axios.get(`${getBaseURL()}/rest/api/2/user/picker?query=${query}`);

export const getAllProjects = (): Promise<AxiosResponse> =>
  axios.get(`${getBaseURL()}/rest/api/2/project`);

export const getIssuesByProjectId = (
  projectId: number
): Promise<AxiosResponse> =>
  axios.get(
    `${getBaseURL()}/rest/api/2/search?jql=project=${projectId}&fields=summary&maxResults=3000`
  );

export const getAllWorklogTypes = (): Promise<AxiosResponse> =>
  axios.get(`${getBaseURL()}/rest/scn-automation/latest/worklog/type`);
