import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services';

/**
 * Auth Guard - Bảo vệ routes yêu cầu authentication
 */
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const isLoggedIn = authService.isLoggedIn();
  console.log('AuthGuard: isLoggedIn =', isLoggedIn);
  console.log('AuthGuard: token =', authService.getToken());
  
  if (isLoggedIn) {
    return true;
  }

  // Redirect to login if not authenticated
  console.log('AuthGuard: Redirecting to /login');
  router.navigate(['/login']);
  return false;
};

/**
 * Login Guard - Chặn user đã login vào trang login/register
 */
export const loginGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isLoggedIn()) {
    return true;
  }

  // Redirect to chat if already authenticated
  router.navigate(['/chat']);
  return false;
};
