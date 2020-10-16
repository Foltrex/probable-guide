import { AutoTTDto } from "../../models";
import {
  ADD_ITEM,
  AutoTTActionType,
  FETCH_ITEMS,
  HIDE_LOADER,
  REMOVE_ITEM,
  SEARCH_ITEMS,
  SHOW_LOADER,
  UPDATE_FORM,
  UPDATE_ITEM,
} from "./types";

export function showLoaderAction(): AutoTTActionType {
  return {
    type: SHOW_LOADER,
  };
}

export function hideLoaderAction(): AutoTTActionType {
  return {
    type: HIDE_LOADER,
  };
}

export function fetchItemsAction(payload: AutoTTDto[]): AutoTTActionType {
  return {
    type: FETCH_ITEMS,
    payload,
  };
}

export function searchItemsAction(payload: string): AutoTTActionType {
  return {
    type: SEARCH_ITEMS,
    payload,
  };
}

export function addItemAction(payload: AutoTTDto): AutoTTActionType {
  return {
    type: ADD_ITEM,
    payload,
  };
}

export function updateItemAction(payload: AutoTTDto): AutoTTActionType {
  return {
    type: UPDATE_ITEM,
    payload,
  };
}

export function removeItemAction(meta: { id: number }): AutoTTActionType {
  return {
    type: REMOVE_ITEM,
    meta,
  };
}

export function updateFormAction(payload: AutoTTDto): AutoTTActionType {
  return {
    type: UPDATE_FORM,
    payload,
  };
}
