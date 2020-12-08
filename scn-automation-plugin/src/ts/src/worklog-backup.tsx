import React from "react";
import ReactDOM from "react-dom";
import Config from "config";
import WorklogBackup from "modules/WorklogBackup";

Config.toInit(() =>
  ReactDOM.render(
    <WorklogBackup />,
    document.getElementById("scn-worklog-backup")
  )
);
