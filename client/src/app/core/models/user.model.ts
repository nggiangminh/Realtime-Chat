/**
 * User Model - Đại diện cho thông tin người dùng
 */
export interface User {
  id: number;
  email: string;
  displayName: string;
  lastSeen?: string;
  createdAt?: string;
}

/**
 * User Status - Trạng thái online/offline
 */
export interface UserStatus {
  userId: number;
  displayName: string;
  status: 'ONLINE' | 'OFFLINE';
}
