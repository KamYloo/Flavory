export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface ApiError {
  success: false;
  message: string;
  error?: string;
  timestamp: string;
}

export type UserRole = "CUSTOMER" | "COOK" | "ADMIN";
export type UserStatus =
  | "ACTIVE"
  | "INACTIVE"
  | "SUSPENDED"
  | "PENDING_VERIFICATION";

export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  phoneNumber?: string;
  profileImageUrl?: string;
  role: UserRole;
  status: UserStatus;
  isVerified: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Address {
  id: number;
  street: string;
  city: string;
  postalCode: string;
  apartmentNumber?: string;
  country: string;
  fullAddress: string;
  isDefault: boolean;
  label?: string;
  latitude?: number;
  longitude?: number;
  createdAt: string;
  phoneNumber?: string;
}

export interface UpdateUserRequest {
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  cookDescription?: string;
  profileImageUrl?: string;
}

export interface CreateAddressRequest {
  street: string;
  city: string;
  postalCode: string;
  apartmentNumber?: string;
  country: string;
  isDefault?: boolean;
  label?: string;
  latitude?: number;
  longitude?: number;
}

export interface UpdateAddressRequest {
  street: string;
  city: string;
  postalCode: string;
  apartmentNumber?: string;
  country: string;
  isDefault?: boolean;
  label?: string;
  latitude?: number;
  longitude?: number;
}

export interface Auth0Credentials {
  accessToken: string;
  idToken: string;
  expiresAt: number;
  refreshToken?: string;
}

export interface AuthState {
  isAuthenticated: boolean;
  isLoading: boolean;
  user: User | null;
  error: string | null;
}
