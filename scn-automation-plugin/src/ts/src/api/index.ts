import axios, { AxiosRequestConfig } from "axios";
import Config from "../config";

axios.defaults.headers = { "Content-Type": "application/json;charset=utf-8" };
Config.toInit(() => (axios.defaults.baseURL = Config.baseURL()));

axios.interceptors.response.use(
  (response) => {
    return Promise.resolve(response);
  },

  (error) => {
    if (error.response.status === Config.FORBIDDEN) {
      return Promise.reject(new Error(error.response.data.errorMessages));
    }

    return Promise.reject(error);
  }
);

export function request<T>(options: AxiosRequestConfig) {
  return axios.request<T>(options);
}
