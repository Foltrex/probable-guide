import axios, { AxiosRequestConfig } from "axios";
import Config from "../config";

interface ResponseData<T> {
  code: number;
  data: T;
  msg: string;
}

axios.defaults.headers = { "Content-Type": "application/json;charset=utf-8" };
Config.toInit(() => (axios.defaults.baseURL = Config.baseURL()));

export function request<T>(options: AxiosRequestConfig) {
  return axios.request<T>(options);
}
