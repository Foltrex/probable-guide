import { AutoTTDto } from "../../models";
import {
  ADD_ITEM,
  AutoTTActionType,
  HIDE_LOADER,
  REMOVE_ITEM,
  SET_ITEM,
  SET_ITEMS,
  SHOW_LOADER,
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

export function setItemsAction(payload: AutoTTDto[]): AutoTTActionType {
  return {
    type: SET_ITEMS,
    payload,
  };
}

export function setItemAction(payload: AutoTTDto): AutoTTActionType {
  return {
    type: SET_ITEM,
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
