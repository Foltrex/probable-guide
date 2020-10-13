import React, { useReducer, useRef } from "react";
import { AddFlagAction, RemoveFlagAction } from "./flagActions";
import { FlagContext } from "./flagContext";
import { flagReducer } from "./flagReducer";

const FlagService = ({ children }) => {
  const [state, dispatch] = useReducer(flagReducer, { flags: [] });
  const flagCount = useRef<number>(1);

  const removeFlag = (): void => dispatch(RemoveFlagAction());
  const addSuccess = (message: string): void =>
    dispatch(
      AddFlagAction({
        id: flagCount.current++,
        appearance: "success",
        message,
      })
    );
  const addError = (message: string): void =>
    dispatch(
      AddFlagAction({
        id: flagCount.current++,
        appearance: "error",
        message,
      })
    );

  return (
    <FlagContext.Provider
      value={{ flags: state.flags, removeFlag, addSuccess, addError }}
    >
      {children}
    </FlagContext.Provider>
  );
};

export default FlagService;
