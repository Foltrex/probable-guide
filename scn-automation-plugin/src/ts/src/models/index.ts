export interface UserDto {
  key: string;
  name?: string;
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
  author?: UserDto;
  updateAuthor?: UserDto;
  created?: number;
  updated?: number;
}

export interface FlagDto {
  id: number;
  appearance: "success" | "error";
  message: string;
}
