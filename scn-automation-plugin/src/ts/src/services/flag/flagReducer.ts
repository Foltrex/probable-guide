import { Reducer } from "react";
import { ADD_FLAG, FlagActionType, REMOVE_FLAG, FlagState } from "../types";

export const flagReducer: Reducer<FlagState, FlagActionType> = (
  state,
  action
) => {
  switch (action.type) {
    case ADD_FLAG:
      return { flags: [action.payload, ...state.flags] };
    case REMOVE_FLAG:
      return { flags: state.flags.slice(1) };
    default:
      return state;
  }
};
