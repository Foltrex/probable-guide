import { Reducer } from "react";
import {
  AutoTTActionType,
  AutoTTState,
  FETCH_ITEMS,
  ADD_ITEM,
  HIDE_LOADER,
  SHOW_LOADER,
  UPDATE_FORM,
  UPDATE_ITEM,
  REMOVE_ITEM,
} from "./types";

export const autoTTReducer: Reducer<AutoTTState, AutoTTActionType> = (
  state,
  action
) => {
  switch (action.type) {
    case SHOW_LOADER:
      return { ...state, isLoaded: false };
    case HIDE_LOADER:
      return { ...state, isLoaded: true };
    case FETCH_ITEMS:
      return { ...state, items: action.payload };
    case ADD_ITEM:
      return { ...state, items: [action.payload, ...state.items] };
    case UPDATE_ITEM:
      return {
        ...state,
        items: state.items.map((item) =>
          item.id === action.payload.id ? action.payload : item
        ),
      };
    case REMOVE_ITEM:
      return {
        ...state,
        items: state.items.filter((item) => item.id !== action.meta.id),
      };
    case UPDATE_FORM:
      return { ...state, formData: action.payload };
    default:
      return state;
  }
};
