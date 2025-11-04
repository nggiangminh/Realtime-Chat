import { Component, Input, OnInit, OnDestroy, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { User, Message } from '../../../core/models';
import { WebSocketService, MessageService } from '../../../core/services';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-chat-window',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat-window.component.html',
  styleUrl: './chat-window.component.css'
})
export class ChatWindowComponent implements OnInit, OnDestroy {
  @Input() selectedUser!: User;
  @Input() currentUser!: User;

  messages = signal<Message[]>([]);
  messageInput = signal<string>('');
  isLoading = signal<boolean>(true);
  isSending = signal<boolean>(false);

  private messageSubscription?: Subscription;

  constructor(
    private webSocketService: WebSocketService,
    private messageService: MessageService
  ) {
    // Watch for selected user changes
    effect(() => {
      if (this.selectedUser) {
        this.loadMessageHistory();
      }
    });
  }

  ngOnInit(): void {
    this.loadMessageHistory();
    this.subscribeToMessages();
  }

  ngOnDestroy(): void {
    this.messageSubscription?.unsubscribe();
  }

  loadMessageHistory(): void {
    if (!this.selectedUser) return;

    this.isLoading.set(true);
    this.messageService.getMessageHistory(this.selectedUser.id).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.messages.set(response.data);
        }
        this.isLoading.set(false);
        this.scrollToBottom();
      },
      error: (error) => {
        console.error('Error loading messages:', error);
        this.isLoading.set(false);
      }
    });
  }

  subscribeToMessages(): void {
    this.messageSubscription = this.webSocketService.messages$.subscribe({
      next: (message) => {
        // Only add message if it's from or to the selected user
        if (
          (message.senderId === this.selectedUser.id && message.receiverId === this.currentUser.id) ||
          (message.senderId === this.currentUser.id && message.receiverId === this.selectedUser.id)
        ) {
          this.messages.update(msgs => [...msgs, message]);
          this.scrollToBottom();
        }
      }
    });
  }

  sendMessage(): void {
    const content = this.messageInput().trim();
    if (!content || this.isSending()) return;

    this.isSending.set(true);
    this.webSocketService.sendMessage(this.selectedUser.id, content);
    this.messageInput.set('');
    this.isSending.set(false);
  }

  onMessageInputChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.messageInput.set(input.value);
  }

  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  isSentByMe(message: Message): boolean {
    return message.senderId === this.currentUser.id;
  }

  formatTime(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
  }

  getUserInitial(displayName: string): string {
    return displayName.charAt(0).toUpperCase();
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      const messagesContainer = document.querySelector('.messages-container');
      if (messagesContainer) {
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
      }
    }, 100);
  }
}
