import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService, WebSocketService, UserService, ThemeService } from '../../core/services';
import { User, UserStatus } from '../../core/models';
import { ChatListComponent } from './chat-list/chat-list.component';
import { ChatWindowComponent } from './chat-window/chat-window.component';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, ChatListComponent, ChatWindowComponent],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css'
})
export class ChatComponent implements OnInit, OnDestroy {
  selectedUser = signal<User | null>(null);
  users = signal<User[]>([]);
  isLoading = signal<boolean>(true);
  userStatusMap = signal<Map<number, 'ONLINE' | 'OFFLINE'>>(new Map());

  private statusSubscription?: Subscription;

  constructor(
    public authService: AuthService,
    public themeService: ThemeService,
    private webSocketService: WebSocketService,
    private userService: UserService,
    private router: Router
  ) {}

  get currentUser() {
    return this.authService.currentUser;
  }

  ngOnInit(): void {
    // Connect to WebSocket
    this.webSocketService.connect();

    // Load users list
    this.loadUsers();

    // Subscribe to user status updates
    this.statusSubscription = this.webSocketService.userStatus$.subscribe({
      next: (status: UserStatus) => {
        console.log('Received user status update:', status);
        const statusMap = this.userStatusMap();
        statusMap.set(status.userId, status.status);
        this.userStatusMap.set(new Map(statusMap));
      },
      error: (error: any) => {
        console.error('Error receiving status update:', error);
      }
    });
  }

  ngOnDestroy(): void {
    // Unsubscribe from status updates
    if (this.statusSubscription) {
      this.statusSubscription.unsubscribe();
    }
    // Disconnect WebSocket when component is destroyed
    this.webSocketService.disconnect();
  }

  loadUsers(): void {
    this.isLoading.set(true);
    this.userService.getAllUsers().subscribe({
      next: (response) => {
        console.log('Load users response:', response);
        // Backend trả về result: "SUCCESS" thay vì success: true
        if ((response.result === 'SUCCESS' || response.success) && response.data) {
          // Filter out current user from the list
          const currentUserId = this.currentUser()?.id;
          const filteredUsers = response.data.filter(u => u.id !== currentUserId);
          console.log('Filtered users:', filteredUsers);
          this.users.set(filteredUsers);
        }
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading users:', error);
        this.isLoading.set(false);
      }
    });
  }

  onUserSelected(user: User): void {
    this.selectedUser.set(user);
  }

  toggleTheme(): void {
    this.themeService.toggleTheme();
  }

  onLogout(): void {
    this.webSocketService.disconnect();
    this.authService.logout();
  }
}
