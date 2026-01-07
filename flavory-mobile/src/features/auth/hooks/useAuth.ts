import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useCallback } from "react";
import { useAuthStore } from "../store/authStore";
import authService from "../services/authService";
import * as userApi from "../api/userApi";
import { CreateAddressRequest, UpdateUserRequest } from "../../../types";

export const authKeys = {
  all: ["auth"] as const,
  currentUser: () => [...authKeys.all, "currentUser"] as const,
  user: (id: number) => [...authKeys.all, "user", id] as const,
  addresses: (userId: number) =>
    [...authKeys.all, "addresses", userId] as const,
  address: (userId: number, addressId: number) =>
    [...authKeys.all, "address", userId, addressId] as const,
};

export const useAuth = () => {
  const queryClient = useQueryClient();
  const {
    user,
    credentials,
    isAuthenticated,
    isLoading,
    error,
    setUser,
    setCredentials,
    setLoading,
    setError,
    clearAuth,
  } = useAuthStore();

  const login = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const creds = await authService.login();
      setCredentials(creds);

      const userData = await userApi.getCurrentUser();
      setUser(userData);

      return creds;
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : "Login failed";
      setError(errorMessage);
      throw err;
    } finally {
      setLoading(false);
    }
  }, [setLoading, setError, setCredentials, setUser]);

  const logout = useCallback(async () => {
    setLoading(true);

    try {
      await authService.logout();
      clearAuth();
      queryClient.clear();
    } catch (err) {
      console.error("Logout error:", err);
      clearAuth();
      queryClient.clear();
    } finally {
      setLoading(false);
    }
  }, [setLoading, clearAuth, queryClient]);

  const refreshToken = useCallback(async () => {
    if (!credentials?.refreshToken) {
      throw new Error("No refresh token available");
    }

    try {
      const newCreds = await authService.refreshToken(credentials.refreshToken);
      setCredentials(newCreds);
      return newCreds;
    } catch (err) {
      await logout();
      throw err;
    }
  }, [credentials, setCredentials, logout]);

  return {
    user,
    credentials,
    isAuthenticated,
    isLoading,
    error,

    login,
    logout,
    refreshToken,
  };
};

export const useCurrentUser = () => {
  const { isAuthenticated } = useAuth();

  return useQuery({
    queryKey: authKeys.currentUser(),
    queryFn: userApi.getCurrentUser,
    enabled: isAuthenticated,
    staleTime: 5 * 60 * 1000,
    retry: 1,
  });
};

export const useUpdateUser = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateUserRequest }) =>
      userApi.updateUser(id, data),
    onSuccess: (updatedUser) => {
      queryClient.setQueryData(authKeys.currentUser(), updatedUser);
      useAuthStore.getState().setUser(updatedUser);
    },
  });
};

export const useUserAddresses = (userId?: number) => {
  return useQuery({
    queryKey: userId ? authKeys.addresses(userId) : ["addresses"],
    queryFn: () => userApi.getUserAddresses(userId!),
    enabled: !!userId,
    staleTime: 2 * 60 * 1000, // 2 minuty
  });
};

export const useCreateAddress = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      userId,
      data,
    }: {
      userId: number;
      data: CreateAddressRequest;
    }) => userApi.createAddress(userId, data),

    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: authKeys.addresses(variables.userId),
      });
    },
  });
};

export const useUpdateAddress = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      userId,
      addressId,
      data,
    }: {
      userId: number;
      addressId: number;
      data: any;
    }) => userApi.updateAddress(userId, addressId, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: authKeys.addresses(variables.userId),
      });
    },
  });
};

export const useDeleteAddress = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      userId,
      addressId,
    }: {
      userId: number;
      addressId: number;
    }) => userApi.deleteAddress(userId, addressId),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: authKeys.addresses(variables.userId),
      });
    },
  });
};
