import { AutoTTDto } from "../../models";

export const SHOW_LOADER = "AUTO_TT/SHOW_LOADER";
export const HIDE_LOADER = "AUTO_TT/HIDE_LOADER";
export const FETCH_ITEMS = "AUTO_TT/FETCH_ITEMS";
export const SEARCH_ITEMS = "AUTO_TT/SEARCH_ITEMS";
export const ADD_ITEM = "AUTO_TT/ADD_ITEM";
export const UPDATE_ITEM = "AUTO_TT/UPDATE_ITEM";
export const REMOVE_ITEM = "AUTO_TT/REMOVE_ITEM";
export const UPDATE_FORM = "AUTO_TT/UPDATE_FORM";

export interface AutoTTState {
  items: AutoTTDto[];
  searchText: string;
  formData: AutoTTDto;
  isLoaded: boolean;
}

interface ShowLoaderAction {
  type: typeof SHOW_LOADER;
}

interface HideLoaderAction {
  type: typeof HIDE_LOADER;
}

interface FetchItemsAction {
  type: typeof FETCH_ITEMS;
  payload: AutoTTDto[];
}

interface SearchItemsAction {
  type: typeof SEARCH_ITEMS;
  payload: string;
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

interface UpdateFormAction {
  type: typeof UPDATE_FORM;
  payload: AutoTTDto;
}

export type AutoTTActionType =
  | ShowLoaderAction
  | HideLoaderAction
  | FetchItemsAction
  | SearchItemsAction
  | AddItemAction
  | UpdateItemAction
  | RemoveItemAction
  | UpdateFormAction;
