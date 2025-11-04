import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, BehaviorSubject } from 'rxjs';
import { Router } from '@angular/router';
import { LoginRequest, RegisterRequest, LoginResponse, ApiResponse, User } from '../models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = 'http://localhost:8083/api/auth';
  private readonly TOKEN_KEY = 'chat_token';
  private readonly USER_KEY = 'chat_user';

  // Signals for reactive state
  private currentUserSubject = new BehaviorSubject<User | null>(this.getUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();
  
  public currentUser = signal<User | null>(this.getUserFromStorage());
  public isAuthenticated = computed(() => this.currentUser() !== null);

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  /**
   * Đăng ký user mới
   */
  register(registerData: RegisterRequest): Observable<ApiResponse<User>> {
    return this.http.post<ApiResponse<User>>(`${this.API_URL}/register`, registerData);
  }

  /**
   * Đăng nhập
   */
  login(loginData: LoginRequest): Observable<ApiResponse<LoginResponse>> {
    return this.http.post<ApiResponse<LoginResponse>>(`${this.API_URL}/login`, loginData)
      .pipe(
        tap(response => {
          if (response.success && response.data) {
            this.setSession(response.data);
          }
        })
      );
  }

  /**
   * Đăng xuất
   */
  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUser.set(null);
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  /**
   * Lấy JWT token
   */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Lấy thông tin user hiện tại
   */
  getCurrentUser(): User | null {
    return this.currentUser();
  }

  /**
   * Kiểm tra đã đăng nhập chưa
   */
  isLoggedIn(): boolean {
    return this.getToken() !== null;
  }

  /**
   * Lưu session sau khi đăng nhập thành công
   */
  private setSession(loginResponse: LoginResponse): void {
    localStorage.setItem(this.TOKEN_KEY, loginResponse.token);
    localStorage.setItem(this.USER_KEY, JSON.stringify(loginResponse.user));
    this.currentUser.set(loginResponse.user);
    this.currentUserSubject.next(loginResponse.user);
  }

  /**
   * Lấy user từ localStorage khi khởi tạo
   */
  private getUserFromStorage(): User | null {
    const userJson = localStorage.getItem(this.USER_KEY);
    return userJson ? JSON.parse(userJson) : null;
  }
}
