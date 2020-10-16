import { createContext } from "react";
import { AutoTTDto } from "../../models";
import { AutoTTState } from "./types";

type ContextProps = AutoTTState & {
  fetchAutoTT(): Promise<void>;
  searchAutoTT(text: string): Promise<void>;
  onCreate(): void;
  onEdit(id: number): Promise<void>;
  onCopy(id: number): Promise<void>;
  addAutoTT(data: AutoTTDto): Promise<void | Object>;
  updateAutoTT(data: AutoTTDto): Promise<void | Object>;
  removeAutoTT(id: number): Promise<void>;
  updateForm(formData: AutoTTDto): void;
};

export const AutoTTContext = createContext<ContextProps>(null);
