import React, { useEffect, useMemo, useState } from "react";
import AutoTTCaption from "../components/AutoTT/AutoTTCaption";
import AutoTTForm from "../components/AutoTT/AutoTTForm";
import AutoTTTable from "../components/AutoTT/AutoTTTable";
import { AutoTTDto } from "../models";
import { useAutoTTService } from "../services/AutoTTService";

const AutoTTContainer: React.FC = () => {
  const {
    items,
    item,
    isLoaded,
    fetchAllAutoTT,
    fetchAutoTT,
    setAutoTT,
    createAutoTT,
    updateAutoTT,
    deleteAutoTT,
  } = useAutoTTService();
  const [searchText, setSearchText] = useState("");

  useEffect(() => {
    fetchAllAutoTT();
  }, []);

  const onCreate = () => {
    setAutoTT({ id: null, ratedTime: "8h", active: true });
  };

  const onClose = () => {
    setAutoTT(null);
  };

  const onEdit = (id: number) => {
    fetchAutoTT(id);
  };

  const onCopy = (id: number) => {
    fetchAutoTT(id).then((value) =>
      setAutoTT({ ...value, id: null, user: null })
    );
  };

  const onSubmit = (data: AutoTTDto) =>
    data.id ? updateAutoTT(data) : createAutoTT(data);

  const visibleItems = useMemo(
    () =>
      items.filter(
        (item) =>
          item.user.key.toLowerCase().includes(searchText.toLowerCase()) ||
          item.user.name.toLowerCase().includes(searchText.toLowerCase()) ||
          item.project.key.toLowerCase().includes(searchText.toLowerCase()) ||
          item.project.name.toLowerCase().includes(searchText.toLowerCase()) ||
          item.issue.key.toLowerCase().includes(searchText.toLowerCase()) ||
          item.issue.name.toLowerCase().includes(searchText.toLowerCase())
      ),
    [items, searchText]
  );

  return (
    <>
      <AutoTTCaption
        onCreate={onCreate}
        searchText={searchText}
        onSearch={(text) => setSearchText(text)}
      />
      <AutoTTForm data={item} onSubmit={onSubmit} onClose={onClose} />
      <AutoTTTable
        items={visibleItems}
        isLoaded={isLoaded}
        onEdit={onEdit}
        onCopy={onCopy}
        onDelete={deleteAutoTT}
      />
    </>
  );
};

export default AutoTTContainer;
