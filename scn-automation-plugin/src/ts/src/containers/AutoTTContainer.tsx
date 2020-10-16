import { search } from "core-js/fn/symbol";
import React, { useContext, useEffect } from "react";
import AutoTTCaption from "../components/AutoTT/AutoTTCaption";
import AutoTTForm from "../components/AutoTT/AutoTTForm";
import AutoTTTable from "../components/AutoTT/AutoTTTable";
import { AutoTTContext } from "../services/autoTT/autoTTContext";

const AutoTTContainer: React.FC = () => {
  const {
    items,
    searchText,
    isLoaded,
    fetchAutoTT,
    onEdit,
    onCopy,
    removeAutoTT,
  } = useContext(AutoTTContext);

  useEffect(() => {
    fetchAutoTT();
  }, []);

  return (
    <>
      <AutoTTCaption />
      <AutoTTForm />
      <AutoTTTable
        items={items.filter(
          (item) =>
            item.user.key.toLowerCase().includes(searchText.toLowerCase()) ||
            item.user.name.toLowerCase().includes(searchText.toLowerCase()) ||
            item.project.key.toLowerCase().includes(searchText.toLowerCase()) ||
            item.project.name
              .toLowerCase()
              .includes(searchText.toLowerCase()) ||
            item.issue.key.toLowerCase().includes(searchText.toLowerCase()) ||
            item.issue.name.toLowerCase().includes(searchText.toLowerCase())
        )}
        isLoaded={isLoaded}
        onEdit={onEdit}
        onCopy={onCopy}
        onDelete={removeAutoTT}
      />
    </>
  );
};

export default AutoTTContainer;
