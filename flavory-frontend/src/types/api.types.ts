export interface ApiResponse<T> {
    success: boolean;
    message?: string;
    data: T;
    timestamp: string;
}

export interface ApiError {
    timestamp: string;
    status: number;
    error: string;
    errorCode: string;
    message: string;
    path: string;
    validationErrors?: ValidationError[];
}

export interface ValidationError {
    field: string;
    message: string;
    rejectedValue?: any;
}