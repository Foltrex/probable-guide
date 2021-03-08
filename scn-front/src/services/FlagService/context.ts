import { createContext } from "react";

type ContextProps = {
  showSuccess: (message: string) => void;
  showInfo: (message: string) => void;
  showError: (message: string) => void;
};

export const FlagContext = createContext<ContextProps>(null);
