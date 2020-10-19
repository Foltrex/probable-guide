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
  const { showSuccess, showInfo, showError } = useFlagService();

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
          showInfo("Data is loaded");
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
          dispatch(addItemAction(result.data));
          dispatch(setItemAction(null));
          showSuccess("Created");
        } catch (error) {
          if (error.response && error.response.status === 403) {
            error.response.data.errorMessages.forEach((message: string) =>
              showError(message)
            );
          }
          if (error.response && error.response.status === 400) {
            return error.response.data.errors;
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
          dispatch(updateItemAction(result.data));
          dispatch(setItemAction(null));
          showSuccess("Updated");
        } catch (error) {
          if (error.response && error.response.status === 403) {
            error.response.data.errorMessages.forEach((message: string) =>
              showError(message)
            );
          }
          if (error.response && error.response.status === 400) {
            return error.response.data.errors;
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
          if (error.response && error.response.status === 403) {
            error.response.data.errorMessages.forEach((message: string) =>
              showError(message)
            );
          }
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
