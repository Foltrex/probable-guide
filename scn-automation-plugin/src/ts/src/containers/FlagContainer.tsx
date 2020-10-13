import { AutoDismissFlag, FlagGroup } from "@atlaskit/flag";
import React, { useContext } from "react";
import SuccessIcon from "@atlaskit/icon/glyph/check-circle";
import ErrorIcon from "@atlaskit/icon/glyph/error";
import { FlagContext } from "../services/flag/flagContext";

const FlagContainer = () => {
  const { flags, removeFlag } = useContext(FlagContext);
  const getIcon = (appearance: string) => {
    switch (appearance) {
      case "success":
        return (
          <SuccessIcon label="Success" size="medium" primaryColor="green" />
        );
      case "error":
        return <ErrorIcon label="Error" size="medium" primaryColor="red" />;
      default:
        return null;
    }
  };
  return (
    <FlagGroup onDismissed={removeFlag}>
      {flags.map((flag) => {
        return (
          <AutoDismissFlag
            id={flag.id}
            icon={getIcon(flag.appearance)}
            key={flag.id}
            title={flag.message}
          />
        );
      })}
    </FlagGroup>
  );
};

export default FlagContainer;
