import { createContext } from "react";
import { AutoTTDto } from "../../models";
import { AutoTTState } from "./types";

type ContextProps = AutoTTState & {
  fetchAllAutoTT(): Promise<void>;
  fetchAutoTT(id: number): Promise<AutoTTDto>;
  setAutoTT(data: AutoTTDto): void;
  createAutoTT(data: AutoTTDto): Promise<void | Object>;
  updateAutoTT(data: AutoTTDto): Promise<void | Object>;
  deleteAutoTT(id: number): Promise<void>;
};

export const AutoTTContext = createContext<ContextProps>(null);
