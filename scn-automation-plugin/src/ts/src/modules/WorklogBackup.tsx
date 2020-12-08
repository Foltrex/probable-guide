import WorklogBackupContainer from "containers/WorklogBackupContainer";
import React from "react";
import FlagService from "services/FlagService";

const WorklogBackup: React.FC = () => {
  return (
    <FlagService>
      <WorklogBackupContainer />
    </FlagService>
  );
};

export default WorklogBackup;
