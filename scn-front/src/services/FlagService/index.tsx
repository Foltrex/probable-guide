import { FlagsProvider, useFlags } from "@atlaskit/flag";
import React, { useContext, useRef } from "react";
import SuccessIcon from "@atlaskit/icon/glyph/check-circle";
import InfoIcon from "@atlaskit/icon/glyph/info";
import ErrorIcon from "@atlaskit/icon/glyph/error";
import { FlagContext } from "./context";

export function useFlagService() {
  const api = useContext(FlagContext);
  if (api == null) {
    throw new Error("Unable to find FlagService");
  }
  return api;
}

const FlagServiceWithoutProvider: React.FC = ({ children }) => {
  const { showFlag } = useFlags();
  const flagCount = useRef<number>(1);

  const api = {
    showSuccess: (message: string) =>
      showFlag({
        id: flagCount.current++,
        title: message,
        icon: (
          <SuccessIcon label="Success" size="medium" primaryColor="green" />
        ),
        isAutoDismiss: true,
      }),
    showInfo: (message: string) =>
      showFlag({
        id: flagCount.current++,
        title: message,
        icon: <InfoIcon label="Info" size="medium" primaryColor="purple" />,
        isAutoDismiss: true,
      }),
    showError: (message: string) =>
      showFlag({
        id: flagCount.current++,
        title: "An error has occurred",
        icon: <ErrorIcon label="Error" size="medium" primaryColor="red" />,
        isAutoDismiss: true,
        description: message,
      }),
  };

  return <FlagContext.Provider value={api}>{children}</FlagContext.Provider>;
};

export default ({ children }) => (
  <FlagsProvider>
    <FlagServiceWithoutProvider>{children}</FlagServiceWithoutProvider>
  </FlagsProvider>
);
