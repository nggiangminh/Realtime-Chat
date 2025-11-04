import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div style="padding: 40px; text-align: center; font-family: sans-serif;">
      <h1 style="color: #7c3aed;">Welcome to RealTime Chat</h1>
      <p style="color: #666; margin: 20px 0;">Please choose an option:</p>
      <div style="display: flex; gap: 16px; justify-content: center; margin-top: 32px;">
        <a 
          routerLink="/login" 
          style="padding: 12px 24px; background: #7c3aed; color: white; text-decoration: none; border-radius: 8px; font-weight: 600;"
        >
          Login
        </a>
        <a 
          routerLink="/register" 
          style="padding: 12px 24px; background: white; color: #7c3aed; text-decoration: none; border-radius: 8px; font-weight: 600; border: 2px solid #7c3aed;"
        >
          Register
        </a>
      </div>
    </div>
  `
})
export class HomeComponent {}
