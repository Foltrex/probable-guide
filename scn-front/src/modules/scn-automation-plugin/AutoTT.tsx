import React from "react";
import AutoTTContainer from "containers/AutoTTContainer";
import AutoTTService from "services/AutoTTService";
import FlagService from "services/FlagService";
import ReactDOM from "react-dom";
import { debugContextDevtool } from "react-context-devtool";

const AutoTT = () => {
  return (
    <FlagService>
      <AutoTTService>
        <AutoTTContainer />
      </AutoTTService>
    </FlagService>
  );
};

window.addEventListener("load", () => {
  const container = document.getElementById("scn-automation-root");
  ReactDOM.render(<AutoTT />, container);
  debugContextDevtool(container);
});
