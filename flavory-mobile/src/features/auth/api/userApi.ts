import apiClient from "../../../api/client";
import {
  Address,
  ApiResponse,
  CreateAddressRequest,
  UpdateAddressRequest,
  UpdateUserRequest,
  User,
} from "../../../types";

export const getCurrentUser = async (): Promise<User> => {
  const response = await apiClient.get<ApiResponse<User>>("/users/me");
  return response.data.data;
};

export const getUserById = async (id: number): Promise<User> => {
  const response = await apiClient.get<ApiResponse<User>>(`/users/${id}`);
  return response.data.data;
};

export const updateUser = async (
  id: number,
  data: UpdateUserRequest,
): Promise<User> => {
  const response = await apiClient.put<ApiResponse<User>>(`/users/${id}`, data);
  return response.data.data;
};

export const createAddress = async (
  userId: number,
  data: CreateAddressRequest,
): Promise<Address> => {
  const response = await apiClient.post<ApiResponse<Address>>(
    `/users/${userId}/addresses`,
    data,
  );
  return response.data.data;
};

export const getAddressById = async (
  userId: number,
  addressId: number,
): Promise<Address> => {
  const response = await apiClient.get<ApiResponse<Address>>(
    `/users/${userId}/addresses/${addressId}`,
  );
  return response.data.data;
};

export const getUserAddresses = async (userId: number): Promise<Address[]> => {
  const response = await apiClient.get<ApiResponse<Address[]>>(
    `/users/${userId}/addresses`,
  );
  return response.data.data;
};

export const updateAddress = async (
  userId: number,
  addressId: number,
  data: UpdateAddressRequest,
): Promise<Address> => {
  const response = await apiClient.put<ApiResponse<Address>>(
    `/users/${userId}/addresses/${addressId}`,
    data,
  );
  return response.data.data;
};

export const setDefaultAddress = async (
  userId: number,
  addressId: number,
): Promise<Address> => {
  const response = await apiClient.patch<ApiResponse<Address>>(
    `/users/${userId}/addresses/${addressId}/default`,
  );
  return response.data.data;
};

export const deleteAddress = async (
  userId: number,
  addressId: number,
): Promise<void> => {
  await apiClient.delete(`/users/${userId}/addresses/${addressId}`);
};
