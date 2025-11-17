export enum UserRole {
    CUSTOMER = 'CUSTOMER',
    COOK = 'COOK',
    ADMIN = 'ADMIN',
}

export enum UserStatus {
    ACTIVE = 'ACTIVE',
    INACTIVE = 'INACTIVE',
    SUSPENDED = 'SUSPENDED',
    PENDING_VERIFICATION = 'PENDING_VERIFICATION',
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
}

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
    cookDescription?: string;
    averageRating?: number;
    totalOrders: number;
    isVerified: boolean;
    addresses: Address[];
    createdAt: string;
    updatedAt: string;
}

export interface UpdateUserRequest {
    firstName?: string;
    lastName?: string;
    phoneNumber?: string;
    cookDescription?: string;
    profileImageUrl?: string;
}

export interface CreateAddressRequest {
    street: string;
    city: string;
    postalCode: string;
    apartmentNumber?: string;
    country?: string;
    isDefault?: boolean;
    label?: string;
    latitude?: number;
    longitude?: number;
}

export interface UpdateAddressRequest {
    street?: string;
    city?: string;
    postalCode?: string;
    apartmentNumber?: string;
    country?: string;
    isDefault?: boolean;
    label?: string;
    latitude?: number;
    longitude?: number;
}