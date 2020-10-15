import React from "react";
import ReactDOM from "react-dom";
import AutoTT from "./modules/AutoTT";

if (window["AJS"]) {
  window["AJS"].toInit(() =>
    ReactDOM.render(<AutoTT />, document.getElementById("scn-index"))
  );
} else {
  ReactDOM.render(<AutoTT />, document.getElementById("scn-index"));
}
