import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User, ApiResponse } from '../models';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly API_URL = 'http://localhost:8083/api/users';

  constructor(private http: HttpClient) {}

  /**
   * Lấy danh sách tất cả users
   */
  getAllUsers(): Observable<ApiResponse<User[]>> {
    return this.http.get<ApiResponse<User[]>>(this.API_URL);
  }

  /**
   * Lấy thông tin chi tiết một user
   */
  getUserById(userId: number): Observable<ApiResponse<User>> {
    return this.http.get<ApiResponse<User>>(`${this.API_URL}/${userId}`);
  }

  /**
   * Tìm kiếm users theo tên
   */
  searchUsers(query: string): Observable<ApiResponse<User[]>> {
    return this.http.get<ApiResponse<User[]>>(`${this.API_URL}/search`, {
      params: { q: query }
    });
  }
}
