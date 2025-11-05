import { Component, Input, Output, EventEmitter, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { User } from '../../../core/models';

@Component({
  selector: 'app-chat-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat-list.component.html',
  styleUrl: './chat-list.component.css'
})
export class ChatListComponent {
  @Input() users: User[] = [];
  @Input() selectedUser: User | null = null;
  @Input() isLoading: boolean = false;
  @Input() userStatusMap: Map<number, 'ONLINE' | 'OFFLINE'> = new Map();
  @Output() userSelected = new EventEmitter<User>();

  searchQuery = signal<string>('');

  get filteredUsers(): User[] {
    const query = this.searchQuery().toLowerCase();
    if (!query) {
      return this.users;
    }
    return this.users.filter(user =>
      user.displayName.toLowerCase().includes(query) ||
      user.email.toLowerCase().includes(query)
    );
  }

  onSearchChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.searchQuery.set(input.value);
  }

  selectUser(user: User): void {
    this.userSelected.emit(user);
  }

  isSelected(user: User): boolean {
    return this.selectedUser?.id === user.id;
  }

  getUserInitial(displayName: string): string {
    return displayName.charAt(0).toUpperCase();
  }

  isUserOnline(userId: number): boolean {
    return this.userStatusMap.get(userId) === 'ONLINE';
  }
}
