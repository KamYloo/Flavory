import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';
import type {
    User,
    UpdateUserRequest,
    Address,
    CreateAddressRequest,
    UpdateAddressRequest
} from '../../types/user.types';
import type { ApiResponse } from '../../types/api.types';

let getAccessTokenFunction: (() => Promise<string>) | null = null;

export const setAccessTokenGetter = (getter: () => Promise<string>) => {
    getAccessTokenFunction = getter;
};

export const userApi = createApi({
    reducerPath: 'userApi',
    baseQuery: fetchBaseQuery({
        baseUrl: import.meta.env.VITE_API_URL,
        prepareHeaders: async (headers) => {
            try {
                if (getAccessTokenFunction) {
                    const token = await getAccessTokenFunction();
                    if (token) {
                        headers.set('Authorization', `Bearer ${token}`);
                    }
                }
            } catch (error) {
                console.error('Error getting token:', error);
            }
            return headers;
        },
    }),
    tagTypes: ['User', 'Address'],
    endpoints: (builder) => ({
        getCurrentUser: builder.query<ApiResponse<User>, void>({
            query: () => '/api/users/me',
            providesTags: ['User'],
        }),

        getUserById: builder.query<ApiResponse<User>, number>({
            query: (id) => `/api/users/${id}`,
            providesTags: (result, error, id) => [{ type: 'User', id }],
        }),

        updateUser: builder.mutation<ApiResponse<User>, { id: number; data: UpdateUserRequest }>({
            query: ({ id, data }) => ({
                url: `/api/users/${id}`,
                method: 'PUT',
                body: data,
            }),
            invalidatesTags: ['User'],
        }),

        deleteUser: builder.mutation<ApiResponse<void>, number>({
            query: (id) => ({
                url: `/api/users/${id}`,
                method: 'DELETE',
            }),
            invalidatesTags: ['User'],
        }),

        getUserAddresses: builder.query<ApiResponse<Address[]>, number>({
            query: (userId) => `/api/users/${userId}/addresses`,
            providesTags: (result, error, userId) =>
                result
                    ? [
                        ...result.data.map(({ id }) => ({ type: 'Address' as const, id })),
                        { type: 'Address', id: 'LIST' },
                    ]
                    : [{ type: 'Address', id: 'LIST' }],
        }),

        createAddress: builder.mutation<ApiResponse<Address>, { userId: number; data: CreateAddressRequest }>({
            query: ({ userId, data }) => ({
                url: `/api/users/${userId}/addresses`,
                method: 'POST',
                body: data,
            }),
            invalidatesTags: [{ type: 'Address', id: 'LIST' }],
        }),

        updateAddress: builder.mutation<ApiResponse<Address>, { userId: number; addressId: number; data: UpdateAddressRequest }>({
            query: ({ userId, addressId, data }) => ({
                url: `/api/users/${userId}/addresses/${addressId}`,
                method: 'PUT',
                body: data,
            }),
            invalidatesTags: (result, error, { addressId }) => [{ type: 'Address', id: addressId }],
        }),

        deleteAddress: builder.mutation<ApiResponse<void>, { userId: number; addressId: number }>({
            query: ({ userId, addressId }) => ({
                url: `/api/users/${userId}/addresses/${addressId}`,
                method: 'DELETE',
            }),
            invalidatesTags: (result, error, { addressId }) => [{ type: 'Address', id: addressId }],
        }),

        setDefaultAddress: builder.mutation<ApiResponse<Address>, { userId: number; addressId: number }>({
            query: ({ userId, addressId }) => ({
                url: `/api/users/${userId}/addresses/${addressId}/default`,
                method: 'PATCH',
            }),
            invalidatesTags: [{ type: 'Address', id: 'LIST' }],
        }),
    }),
});

export const {
    useGetCurrentUserQuery,
    useGetUserByIdQuery,
    useUpdateUserMutation,
    useDeleteUserMutation,
    useGetUserAddressesQuery,
    useCreateAddressMutation,
    useUpdateAddressMutation,
    useDeleteAddressMutation,
    useSetDefaultAddressMutation,
} = userApi;