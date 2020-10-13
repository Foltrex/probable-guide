import { FlagDto } from "../models";

export const ADD_FLAG = "APP/ADD_FLAG";
export const REMOVE_FLAG = "APP/REMOVE_FLAG";

export interface FlagState {
  flags: Array<FlagDto>;
}

interface AddFlagAction {
  type: typeof ADD_FLAG;
  payload: FlagDto;
}

interface RemoveFlagAction {
  type: typeof REMOVE_FLAG;
}

export type FlagActionType = AddFlagAction | RemoveFlagAction;
