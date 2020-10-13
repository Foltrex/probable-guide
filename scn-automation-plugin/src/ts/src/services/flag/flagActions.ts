import { FlagDto } from "../../models";
import { ADD_FLAG, FlagActionType, REMOVE_FLAG } from "../types";

export function AddFlagAction(payload: FlagDto): FlagActionType {
  return {
    type: ADD_FLAG,
    payload: payload,
  };
}

export function RemoveFlagAction(): FlagActionType {
  return { type: REMOVE_FLAG };
}
