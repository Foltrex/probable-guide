import WorklogBackupContainer from "containers/WorklogBackupContainer";
import React from "react";
import ReactDOM from "react-dom";
import FlagService from "services/FlagService";

const WorklogBackup: React.FC = () => {
  return (
    <FlagService>
      <WorklogBackupContainer />
    </FlagService>
  );
};

window.addEventListener("load", () =>
  ReactDOM.render(
    <WorklogBackup />,
    document.getElementById("scn-automation-root")
  )
);
