import Config from "../config";

export const getUserAvatarURL = (key: string): string =>
  `${Config.baseURL()}/secure/useravatar?ownerId=${key}`;

export const getUserProfileURL = (key: string): string =>
  `${Config.baseURL()}/secure/ViewProfile.jspa?name=${key}`;

export const getProjectOrIssueURL = (key: string): string =>
  `${Config.baseURL()}/browse/${key}`;
