import { User } from './user.model';

/**
 * Login Request - Payload cho API đăng nhập
 */
export interface LoginRequest {
  email: string;
  password: string;
}

/**
 * Register Request - Payload cho API đăng ký
 */
export interface RegisterRequest {
  email: string;
  password: string;
  displayName: string;
}

/**
 * Login Response - Kết quả từ API đăng nhập
 */
export interface LoginResponse {
  token: string;
  user: User;
}

/**
 * API Response Wrapper - Cấu trúc chung cho mọi API response
 */
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data?: T;
  timestamp?: string;
}
