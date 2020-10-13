import DynamicTable from "@atlaskit/dynamic-table";
import { ModalTransition } from "@atlaskit/modal-dialog";
import React, { useContext, useEffect, useState } from "react";
import {
  getAllAutoTT,
  getAutoTT,
  removeAutoTT,
  updateAutoTT,
  addAutoTT,
} from "../../api";
import { AutoTTDto } from "../../models";
import { FlagContext } from "../../services/flag/flagContext";
import AutoTTCaption from "./AutoTTCaption";
import AutoTTDialog from "./AutoTTDialog";
import { getRows, head } from "./data";

const AutoTTTable: React.FC = () => {
  const [autoTTList, setAutoTTList] = useState<AutoTTDto[]>([]);
  const [isLoaded, setIsLoaded] = useState<boolean>(false);
  const [isFormOpened, setIsFormOpened] = useState<boolean>(false);
  const [formData, setFormData] = useState<AutoTTDto>({ id: null });
  const { addSuccess, addError } = useContext(FlagContext);

  useEffect(() => {
    getAllAutoTT()
      .then((result) => {
        setIsLoaded(true);
        setAutoTTList(result.data);
        addSuccess("Data is loaded");
      })
      .catch(({ message }) => {
        setIsLoaded(true);
        addError(message);
      });
  }, []);

  const onSubmitCreate = (data: AutoTTDto) => {
    addAutoTT(data)
      .then((result) => {
        setAutoTTList((prev) => [result.data, ...prev]);
        setIsFormOpened(false);
        addSuccess("Created");
      })
      .catch(({ message }) => addError(message));
  };

  const onSubmitUpdate = (data: AutoTTDto) => {
    updateAutoTT(data)
      .then((result) => {
        setAutoTTList(
          autoTTList.map((value) =>
            value.id === data.id ? result.data : value
          )
        );
        setIsFormOpened(false);
        addSuccess("Updated");
      })
      .catch(({ message }) => addError(message));
  };

  const createAction = () => {
    setIsFormOpened(true);
    setFormData({ id: null, active: true });
  };

  const editAction = (id: number) => {
    getAutoTT(id)
      .then((result) => {
        setFormData(result.data);
        setIsFormOpened(true);
      })
      .catch(({ message }) => addError(message));
  };

  const copyAction = (id: number) => {
    getAutoTT(id).then((result) => {
      setFormData((prev) => ({
        ...prev,
        ...result.data,
        id: null,
        user: null,
      }));
      setIsFormOpened(true);
    });
  };

  const deleteAction = (id: number) => {
    removeAutoTT(id)
      .then(() => {
        setAutoTTList(
          autoTTList.filter((autoTT: AutoTTDto) => autoTT.id !== id)
        );
        addSuccess("Deleted");
      })
      .catch(({ message }) => addError(message));
  };

  return (
    <>
      <AutoTTCaption
        caption="Auto time tracking users"
        createAction={createAction}
      />
      <DynamicTable
        emptyView={<h2>No elements</h2>}
        isLoading={!isLoaded}
        head={head}
        rows={getRows(autoTTList, editAction, copyAction, deleteAction)}
        loadingSpinnerSize="large"
      />
      <ModalTransition>
        {isFormOpened && (
          <AutoTTDialog
            heading={
              formData.id
                ? "Edit auto time tracking"
                : "Create auto time tracking"
            }
            onClose={setIsFormOpened.bind(this, false)}
            onSubmit={formData.id ? onSubmitUpdate : onSubmitCreate}
            data={formData}
          />
        )}
      </ModalTransition>
    </>
  );
};

export default AutoTTTable;
