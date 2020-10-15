import React, { useContext, useEffect } from "react";
import AutoTTCaption from "../components/AutoTT/AutoTTCaption";
import AutoTTForm from "../components/AutoTT/AutoTTForm";
import AutoTTTable from "../components/AutoTT/AutoTTTable";
import { AutoTTContext } from "../services/autoTT/autoTTContext";

const AutoTTContainer: React.FC = () => {
  const {
    items,
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
        items={items}
        isLoaded={isLoaded}
        onEdit={onEdit}
        onCopy={onCopy}
        onDelete={removeAutoTT}
      />
    </>
  );
};

export default AutoTTContainer;
