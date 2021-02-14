import React, { useContext, useMemo, useReducer } from "react";
import { request } from "../../api";
import {
  addItemAction,
  hideLoaderAction,
  removeItemAction,
  setItemAction,
  setItemsAction,
  showLoaderAction,
  updateItemAction,
} from "./actions";
import { AutoTTContext } from "./context";
import { autoTTReducer } from "./reducer";
import { AutoTTDto } from "../../models";
import { useFlagService } from "../FlagService";
import Config from "../../config";

export function useAutoTTService() {
  const api = useContext(AutoTTContext);
  if (api == null) {
    throw new Error("Unable to find AutoTTService");
  }
  return api;
}

const AutoTTService: React.FC = ({ children }) => {
  const [state, dispatch] = useReducer(autoTTReducer, {
    items: [],
    item: null,
    isLoaded: true,
  });
  const { showSuccess, showError } = useFlagService();

  const api = useMemo(
    () => ({
      async fetchAllAutoTT(): Promise<void> {
        dispatch(showLoaderAction());
        try {
          const result = await request<AutoTTDto[]>({
            url: `${Config.API}/autotimetracking/user`,
            method: "GET",
          });
          dispatch(setItemsAction(result.data));
        } catch (error) {
          showError(error.message);
        }
        dispatch(hideLoaderAction());
      },
      async fetchAutoTT(id: number): Promise<AutoTTDto> {
        try {
          const result = await request<AutoTTDto>({
            url: `${Config.API}/autotimetracking/user/${id}`,
            method: "GET",
          });
          dispatch(setItemAction(result.data));
          return result.data;
        } catch (error) {
          showError(error.message);
        }
        return null;
      },
      setAutoTT(data: AutoTTDto): void {
        dispatch(setItemAction(data));
      },
      async createAutoTT(data: AutoTTDto): Promise<void | Object> {
        try {
          const result = await request<AutoTTDto>({
            url: `${Config.API}/autotimetracking/user`,
            method: "POST",
            data: data,
          });
          dispatch(setItemAction(null));
          dispatch(addItemAction(result.data));
          showSuccess("Created");
        } catch (error) {
          if (error.response && error.response.status === Config.BAD_REQUEST) {
            return error.response.data.errors;
          } else {
            showError(error.message);
          }
        }
      },
      async updateAutoTT(data: AutoTTDto): Promise<void | Object> {
        try {
          const result = await request<AutoTTDto>({
            url: `${Config.API}/autotimetracking/user`,
            method: "PUT",
            data: data,
          });
          dispatch(setItemAction(null));
          dispatch(updateItemAction(result.data));
          showSuccess("Updated");
        } catch (error) {
          if (error.response && error.response.status === Config.BAD_REQUEST) {
            return error.response.data.errors;
          } else {
            showError(error.message);
          }
        }
      },
      async deleteAutoTT(id: number): Promise<void> {
        try {
          await request<AutoTTDto>({
            url: `${Config.API}/autotimetracking/user/${id}`,
            method: "DELETE",
          });
          dispatch(removeItemAction({ id }));
        } catch (error) {
          showError(error.message);
        }
      },
    }),
    []
  );

  return (
    <AutoTTContext.Provider
      value={{
        ...state,
        ...api,
      }}
    >
      {children}
    </AutoTTContext.Provider>
  );
};

export default AutoTTService;
