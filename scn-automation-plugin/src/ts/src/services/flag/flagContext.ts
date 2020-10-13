import { createContext } from "react";
import { FlagDto } from "../../models";

type ContextProps = {
  flags: Array<FlagDto>;
  addSuccess: (message: string) => void;
  addError: (message: string) => void;
  removeFlag: () => void;
};

export const FlagContext = createContext<Partial<ContextProps>>({});
