import { FlagsProvider, useFlags } from "@atlaskit/flag";
import React, { useRef } from "react";
import SuccessIcon from "@atlaskit/icon/glyph/check-circle";
import InfoIcon from "@atlaskit/icon/glyph/info";
import ErrorIcon from "@atlaskit/icon/glyph/error";
import { FlagContext } from "./flagContext";

const FlagService: React.FC = ({ children }) => {
  const { showFlag } = useFlags();
  const flagCount = useRef<number>(1);

  const showSuccess = (message: string) =>
    showFlag({
      id: flagCount.current++,
      title: message,
      icon: <SuccessIcon label="Success" size="medium" primaryColor="green" />,
      isAutoDismiss: true,
    });
  const showInfo = (message: string) =>
    showFlag({
      id: flagCount.current++,
      title: message,
      icon: <InfoIcon label="Info" size="medium" primaryColor="purple" />,
      isAutoDismiss: true,
    });
  const showError = (message: string) =>
    showFlag({
      id: flagCount.current++,
      title: message,
      icon: <ErrorIcon label="Error" size="medium" primaryColor="red" />,
      isAutoDismiss: true,
    });

  return (
    <FlagContext.Provider value={{ showSuccess, showInfo, showError }}>
      {children}
    </FlagContext.Provider>
  );
};

export default ({ children }) => (
  <FlagsProvider>
    <FlagService>{children}</FlagService>
  </FlagsProvider>
);
