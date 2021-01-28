import { AutoTTDto } from "../../models";

export const SHOW_LOADER = "AUTO_TT/SHOW_LOADER";
export const HIDE_LOADER = "AUTO_TT/HIDE_LOADER";
export const SET_ITEMS = "AUTO_TT/SET_ITEMS";
export const SET_ITEM = "AUTO_TT/SET_ITEM";
export const ADD_ITEM = "AUTO_TT/ADD_ITEM";
export const UPDATE_ITEM = "AUTO_TT/UPDATE_ITEM";
export const REMOVE_ITEM = "AUTO_TT/REMOVE_ITEM";

export interface AutoTTState {
  items: AutoTTDto[];
  item: AutoTTDto;
  isLoaded: boolean;
}

interface ShowLoaderAction {
  type: typeof SHOW_LOADER;
}

interface HideLoaderAction {
  type: typeof HIDE_LOADER;
}

interface SetItemsAction {
  type: typeof SET_ITEMS;
  payload: AutoTTDto[];
}

interface SetItemAction {
  type: typeof SET_ITEM;
  payload: AutoTTDto;
}

interface AddItemAction {
  type: typeof ADD_ITEM;
  payload: AutoTTDto;
}

interface UpdateItemAction {
  type: typeof UPDATE_ITEM;
  payload: AutoTTDto;
}

interface RemoveItemAction {
  type: typeof REMOVE_ITEM;
  meta: { id: number };
}

export type AutoTTActionType =
  | ShowLoaderAction
  | HideLoaderAction
  | SetItemsAction
  | SetItemAction
  | SetItemAction
  | AddItemAction
  | UpdateItemAction
  | RemoveItemAction;
