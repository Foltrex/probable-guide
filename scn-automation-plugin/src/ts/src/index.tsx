import React from "react";
import ReactDOM from "react-dom";
import AutoTimetracking from "./components/AutoTimeTracking";

if (window["AJS"]) {
  window["AJS"].toInit(() =>
    ReactDOM.render(<AutoTimetracking />, document.getElementById("scn-index"))
  );
} else {
  ReactDOM.render(<AutoTimetracking />, document.getElementById("scn-index"));
}
