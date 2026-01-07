import { create } from "zustand";
import { Auth0Credentials, User } from "../../../types";

interface AuthStore {
  user: User | null;
  credentials: Auth0Credentials | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;

  setUser: (user: User | null) => void;
  setCredentials: (credentials: Auth0Credentials | null) => void;
  setLoading: (isLoading: boolean) => void;
  setError: (error: string | null) => void;
  clearAuth: () => void;
  reset: () => void;
}

const initialState = {
  user: null,
  credentials: null,
  isAuthenticated: false,
  isLoading: false,
  error: null,
};

export const useAuthStore = create<AuthStore>((set) => ({
  ...initialState,

  setUser: (user) =>
    set({
      user,
      isAuthenticated: !!user,
      error: null,
    }),

  setCredentials: (credentials) =>
    set({
      credentials,
      error: null,
    }),

  setLoading: (isLoading) =>
    set({
      isLoading,
    }),

  setError: (error) =>
    set({
      error,
      isLoading: false,
    }),

  clearAuth: () =>
    set({
      user: null,
      credentials: null,
      isAuthenticated: false,
      error: null,
    }),

  reset: () =>
    set({
      ...initialState,
    }),
}));

export const selectUser = (state: AuthStore) => state.user;
export const selectIsAuthenticated = (state: AuthStore) =>
  state.isAuthenticated;
export const selectIsLoading = (state: AuthStore) => state.isLoading;
export const selectError = (state: AuthStore) => state.error;
