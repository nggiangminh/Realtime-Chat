import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Message, ApiResponse } from '../models';

@Injectable({
  providedIn: 'root'
})
export class MessageService {
  private readonly API_URL = 'http://localhost:8083/api/messages';

  constructor(private http: HttpClient) {}

  /**
   * Lấy lịch sử chat với một user
   */
  getMessageHistory(userId: number): Observable<ApiResponse<Message[]>> {
    return this.http.get<ApiResponse<Message[]>>(`${this.API_URL}/${userId}`);
  }

  /**
   * Đánh dấu tin nhắn đã đọc
   */
  markAsRead(messageId: number): Observable<ApiResponse<void>> {
    return this.http.put<ApiResponse<void>>(`${this.API_URL}/${messageId}/read`, {});
  }

  /**
   * Đánh dấu tất cả tin nhắn từ một user là đã đọc
   */
  markAllAsRead(userId: number): Observable<ApiResponse<void>> {
    return this.http.put<ApiResponse<void>>(`${this.API_URL}/user/${userId}/read-all`, {});
  }
}
