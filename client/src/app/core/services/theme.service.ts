import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private readonly THEME_KEY = 'chat_theme';
  
  // Signal for current theme
  isDarkMode = signal<boolean>(this.getInitialTheme());

  constructor() {
    // Apply initial theme
    this.applyTheme(this.isDarkMode());
  }

  /**
   * Toggle between light and dark mode
   */
  toggleTheme(): void {
    const newTheme = !this.isDarkMode();
    this.isDarkMode.set(newTheme);
    this.applyTheme(newTheme);
    this.saveTheme(newTheme);
  }

  /**
   * Apply theme to document
   */
  private applyTheme(isDark: boolean): void {
    if (isDark) {
      document.documentElement.classList.add('dark-mode');
    } else {
      document.documentElement.classList.remove('dark-mode');
    }
  }

  /**
   * Get initial theme from localStorage or system preference
   */
  private getInitialTheme(): boolean {
    const savedTheme = localStorage.getItem(this.THEME_KEY);
    if (savedTheme !== null) {
      return savedTheme === 'dark';
    }
    // Check system preference
    return window.matchMedia('(prefers-color-scheme: dark)').matches;
  }

  /**
   * Save theme preference to localStorage
   */
  private saveTheme(isDark: boolean): void {
    localStorage.setItem(this.THEME_KEY, isDark ? 'dark' : 'light');
  }
}
