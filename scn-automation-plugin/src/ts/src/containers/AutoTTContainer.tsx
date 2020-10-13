import React from "react";
import AutoTTTable from "../components/AutoTimeTracking";
import FlagService from "../services/flag/FlagService";
import FlagContainer from "./FlagContainer";

const AutoTTContainer = () => {
  return (
    <FlagService>
      <FlagContainer />
      <AutoTTTable />
    </FlagService>
  );
};

export default AutoTTContainer;
