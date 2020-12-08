import React from "react";
import AutoTTContainer from "containers/AutoTTContainer";
import AutoTTService from "services/AutoTTService";
import FlagService from "services/FlagService";

const AutoTT = () => {
  return (
    <FlagService>
      <AutoTTService>
        <AutoTTContainer />
      </AutoTTService>
    </FlagService>
  );
};

export default AutoTT;
