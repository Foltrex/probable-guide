import React, { useContext, useReducer } from "react";
import {
  getAllAutoTT,
  getAutoTT,
  putAutoTT,
  postAutoTT,
  deleteAutoTT,
} from "../../api";
import {
  addItemAction,
  fetchItemsAction,
  hideLoaderAction,
  removeItemAction,
  showLoaderAction,
  updateFormAction,
  updateItemAction,
} from "./autoTTActions";
import { AutoTTContext } from "./autoTTContext";
import { autoTTReducer } from "./autoTTReducer";
import { FlagContext } from "../flag/flagContext";
import { AutoTTDto } from "../../models";

const AutoTTService: React.FC = ({ children }) => {
  const [state, dispatch] = useReducer(autoTTReducer, {
    items: [],
    formData: null,
    isLoaded: true,
  });
  const { showSuccess, showInfo, showError } = useContext(FlagContext);

  const fetchAutoTT = async (): Promise<void> => {
    dispatch(showLoaderAction());
    try {
      const result = await getAllAutoTT();
      dispatch(fetchItemsAction(result.data));
      showInfo("Data is loaded");
    } catch (error) {
      showError(error.message);
    }
    dispatch(hideLoaderAction());
  };
  const updateForm = (formData: AutoTTDto): void =>
    dispatch(updateFormAction(formData));
  const onCreate = (): void =>
    updateForm({ id: null, ratedTime: "8h", active: true });
  const onEdit = async (id: number): Promise<void> => {
    try {
      const result = await getAutoTT(id);
      updateForm(result.data);
    } catch (error) {
      showError(error.message);
    }
  };
  const onCopy = async (id: number): Promise<void> => {
    try {
      const result = await getAutoTT(id);
      updateForm(result.data);
    } catch (error) {
      showError(error.message);
    }
  };
  const addAutoTT = async (data: AutoTTDto): Promise<void | Object> => {
    try {
      const result = await postAutoTT(data);
      updateForm(null);
      dispatch(addItemAction(result.data));
      showSuccess("Created");
    } catch (error) {
      showError(error.message);
      if (error.response && error.response.status === 400) {
        return error.response.data.errors;
      }
    }
  };
  const updateAutoTT = async (data: AutoTTDto): Promise<void | Object> => {
    try {
      const result = await putAutoTT(data);
      updateForm(null);
      dispatch(updateItemAction(result.data));
      showSuccess("Updated");
    } catch (error) {
      showError(error.message);
      if (error.response && error.response.status === 400) {
        return error.response.data.errors;
      }
    }
  };
  const removeAutoTT = async (id: number): Promise<void> => {
    try {
      await deleteAutoTT(id);
      dispatch(removeItemAction({ id }));
    } catch (error) {
      showError(error.message);
    }
  };

  return (
    <AutoTTContext.Provider
      value={{
        ...state,
        fetchAutoTT,
        onCreate,
        onEdit,
        onCopy,
        addAutoTT,
        updateAutoTT,
        removeAutoTT,
        updateForm,
      }}
    >
      {children}
    </AutoTTContext.Provider>
  );
};

export default AutoTTService;
