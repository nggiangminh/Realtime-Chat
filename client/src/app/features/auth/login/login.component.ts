import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  loginForm: FormGroup;
  errorMessage = signal<string>('');
  isLoading = signal<boolean>(false);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set('');

    const { email, password } = this.loginForm.value;

    this.authService.login({ email, password }).subscribe({
      next: (response) => {
        console.log('Login response:', response);
        this.isLoading.set(false);
        // Backend trả về result: "SUCCESS" thay vì success: true
        if (response.result === 'SUCCESS' || response.success) {
          console.log('Login successful, navigating to /chat');
          this.router.navigate(['/chat']);
        } else {
          this.errorMessage.set(response.message || 'Đăng nhập thất bại');
        }
      },
      error: (error) => {
        console.error('Login error:', error);
        this.isLoading.set(false);
        this.errorMessage.set(error.error?.message || 'Đăng nhập thất bại. Vui lòng thử lại.');
      }
    });
  }

  get email() {
    return this.loginForm.get('email');
  }

  get password() {
    return this.loginForm.get('password');
  }
}
