import { Component, Input, OnInit, OnDestroy, OnChanges, SimpleChanges, signal } from '@angular/core';
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
export class ChatWindowComponent implements OnInit, OnDestroy, OnChanges {
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
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    // Detect when selectedUser changes
    if (changes['selectedUser'] && !changes['selectedUser'].firstChange) {
      const newUser = changes['selectedUser'].currentValue;
      console.log('Selected user changed to:', newUser?.displayName);
      
      // Clear current messages
      this.messages.set([]);
      
      // Load new chat history
      if (newUser) {
        this.loadMessageHistory();
      }
    }
  }

  ngOnInit(): void {
    // Load initial message history
    if (this.selectedUser) {
      this.loadMessageHistory();
    }
    // Subscribe to real-time messages
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
        console.log('Message history response:', response);
        // Backend trả về result: "SUCCESS" hoặc success: true
        if ((response.result === 'SUCCESS' || response.success) && response.data) {
          console.log('Loaded messages:', response.data);
          this.messages.set(response.data);
        } else {
          console.log('No messages or failed:', response);
          this.messages.set([]);
        }
        this.isLoading.set(false);
        this.scrollToBottom();
      },
      error: (error) => {
        console.error('Error loading messages:', error);
        this.messages.set([]);
        this.isLoading.set(false);
      }
    });
  }

  subscribeToMessages(): void {
    this.messageSubscription = this.webSocketService.messages$.subscribe({
      next: (message) => {
        console.log('Received WebSocket message:', message);
        
        // Only add message if it's from or to the selected user
        if (
          (message.senderId === this.selectedUser.id && message.receiverId === this.currentUser.id) ||
          (message.senderId === this.currentUser.id && message.receiverId === this.selectedUser.id)
        ) {
          // Check for duplicate before adding
          const exists = this.messages().some(m => m.id === message.id);
          if (!exists) {
            console.log('Adding message to UI:', message);
            this.messages.update(msgs => [...msgs, message]);
            this.scrollToBottom();
          } else {
            console.log('Message already exists, skipping:', message.id);
          }
        }
      }
    });
  }

  sendMessage(): void {
    const content = this.messageInput().trim();
    if (!content || this.isSending()) return;

    this.isSending.set(true);
    
    console.log('Sending message:', content);
    
    // Clear input immediately for better UX
    this.messageInput.set('');
    
    // Send via WebSocket
    this.webSocketService.sendMessage(this.selectedUser.id, content);
    
    // Reset sending state after a short delay
    setTimeout(() => {
      this.isSending.set(false);
    }, 500);
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
