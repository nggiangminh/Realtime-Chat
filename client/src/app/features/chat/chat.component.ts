import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService, WebSocketService, UserService } from '../../core/services';
import { User } from '../../core/models';
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

  constructor(
    public authService: AuthService,
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
  }

  ngOnDestroy(): void {
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

  onLogout(): void {
    this.webSocketService.disconnect();
    this.authService.logout();
  }
}
