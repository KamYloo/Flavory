import axios, { AxiosError, InternalAxiosRequestConfig } from "axios";
import ENV from "../config/env";
import { ApiError } from "../types";

export const apiClient = axios.create({
  baseURL: ENV.API_URL,
  timeout: 15000,
  headers: {
    "Content-Type": "application/json",
    Accept: "application/json",
  },
});

let accessToken: string | null = null;

export const setAccessToken = (token: string | null): void => {
  accessToken = token;
};

export const getAccessToken = (): string | null => {
  return accessToken;
};

export const clearAccessToken = (): void => {
  accessToken = null;
};

apiClient.interceptors.request.use(
  async (config: InternalAxiosRequestConfig) => {
    const token = getAccessToken();

    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    if (ENV.AUTH0_AUDIENCE && config.headers) {
      config.headers["X-Auth0-Audience"] = ENV.AUTH0_AUDIENCE;
    }

    if (__DEV__) {
      console.log("🚀 API Request:", {
        method: config.method?.toUpperCase(),
        url: config.url,
        data: config.data,
      });
    }

    return config;
  },
  (error: AxiosError) => {
    if (__DEV__) {
      console.error("❌ Request Error:", error);
    }
    return Promise.reject(error);
  },
);

apiClient.interceptors.response.use(
  (response) => {
    if (__DEV__) {
      console.log("✅ API Response:", {
        status: response.status,
        url: response.config.url,
        data: response.data,
      });
    }
    return response;
  },
  async (error: AxiosError<ApiError>) => {
    if (__DEV__) {
      console.error("❌ API Error:", {
        status: error.response?.status,
        url: error.config?.url,
        message: error.response?.data?.message || error.message,
      });
    }

    if (error.response?.status === 401) {
      clearAccessToken();
    }

    if (error.response?.status === 403) {
      console.warn("⚠️ Access forbidden - insufficient permissions");
    }

    if (error.response?.status === 500) {
      console.error("🔥 Server Error - please try again later");
    }

    return Promise.reject(error);
  },
);

export const getErrorMessage = (error: unknown): string => {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<ApiError>;
    return (
      axiosError.response?.data?.message ||
      axiosError.message ||
      "An unexpected error occurred"
    );
  }

  if (error instanceof Error) {
    return error.message;
  }

  return "An unexpected error occurred";
};

export const isNetworkError = (error: unknown): boolean => {
  if (axios.isAxiosError(error)) {
    return !error.response && error.code === "ERR_NETWORK";
  }
  return false;
};

export default apiClient;
