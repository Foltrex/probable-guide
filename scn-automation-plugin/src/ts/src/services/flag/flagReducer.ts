import { Reducer } from "react";
import { ADD_FLAG, FlagActionType, REMOVE_FLAG, FlagState } from "../types";

export const flagReducer: Reducer<FlagState, FlagActionType> = (
  state,
  action
) => {
  switch (action.type) {
    case ADD_FLAG:
      console.log(action.payload.id);
      return { flags: [action.payload, ...state.flags] };
    case REMOVE_FLAG:
      return { flags: state.flags.slice(1) };
    default:
      return state;
  }
};
