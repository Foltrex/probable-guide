import React from "react";
import ReactDOM from "react-dom";
import AutoTTContainer from "./containers/AutoTTContainer";

if (window["AJS"]) {
  window["AJS"].toInit(() =>
    ReactDOM.render(<AutoTTContainer />, document.getElementById("scn-index"))
  );
} else {
  ReactDOM.render(<AutoTTContainer />, document.getElementById("scn-index"));
}
