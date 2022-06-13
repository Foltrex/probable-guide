import axios, { AxiosRequestConfig } from "axios";
import Config from "../config";

axios.defaults.headers = { "Content-Type": "application/json;charset=utf-8" };
window.addEventListener(
  "load",
  () => (axios.defaults.baseURL = Config.baseURL())
);

axios.interceptors.response.use(
  (response) => Promise.resolve(response),
  (error) => Promise.reject(error)
);

export function request<T>(options: AxiosRequestConfig) {
  return axios.request<T>(options);
}
