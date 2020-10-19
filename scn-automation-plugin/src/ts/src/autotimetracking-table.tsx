import React from "react";
import ReactDOM from "react-dom";
import Config from "./config";
import AutoTT from "./modules/AutoTT";

Config.toInit(() =>
  ReactDOM.render(
    <AutoTT />,
    document.getElementById("scn-autotimetracking-table")
  )
);
