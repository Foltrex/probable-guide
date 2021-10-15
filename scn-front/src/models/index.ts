export interface UserDto {
  key: string;
  name?: string;
  username?: string;
}

export interface ProjectDto {
  id: number;
  key?: string;
  name?: string;
}

export interface IssueDto {
  id: number;
  key?: string;
  name?: string;
  displayHtml?: string;
}

export interface WorklogTypeDto {
  id: string;
  name?: string;
}

export interface AutoTTDto {
  id: number;
  user?: UserDto;
  active?: boolean;
  project?: ProjectDto;
  issue?: IssueDto;
  worklogType?: WorklogTypeDto;
  ratedTime?: string;
  author?: UserDto;
  updateAuthor?: UserDto;
  created?: number;
  updated?: number;
}
