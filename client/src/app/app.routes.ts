import { Routes } from '@angular/router';
import { authGuard, loginGuard } from './core/guards';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { ChatComponent } from './features/chat/chat.component';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },
  {
    path: 'login',
    component: LoginComponent,
    canActivate: [loginGuard]
  },
  {
    path: 'register',
    component: RegisterComponent,
    canActivate: [loginGuard]
  },
  {
    path: 'chat',
    component: ChatComponent,
    canActivate: [authGuard]
  },
  {
    path: '**',
    redirectTo: 'login'
  }
];
