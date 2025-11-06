import { Component, Input, Output, EventEmitter, OnInit, OnDestroy, OnChanges, SimpleChanges, signal, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { User, Message, ReactionResponse } from '../../../core/models';
import { WebSocketService, MessageService, FileUploadService } from '../../../core/services';
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
  @Input() userStatusMap: Map<number, 'ONLINE' | 'OFFLINE'> = new Map();
  @Output() backToList = new EventEmitter<void>();

  messages = signal<Message[]>([]);
  messageInput = signal<string>('');
  isLoading = signal<boolean>(true);
  isSending = signal<boolean>(false);
  isUploading = signal<boolean>(false);
  selectedImage = signal<File | null>(null);
  imagePreviewUrl = signal<string | null>(null);
  showReactionPicker = signal<number | null>(null); // messageId hoặc null

  // Danh sách emoji phổ biến
  popularEmojis = ['👍', '❤️', '😂', '😮', '😢', '🙏'];

  private messageSubscription?: Subscription;
  private reactionSubscription?: Subscription;
  private messageDeleteSubscription?: Subscription;

  constructor(
    private webSocketService: WebSocketService,
    private messageService: MessageService,
    private fileUploadService: FileUploadService
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
    // Subscribe to reactions
    this.subscribeToReactions();
    // Subscribe to message deletes
    this.subscribeToMessageDeletes();
  }

  ngOnDestroy(): void {
    this.messageSubscription?.unsubscribe();
    this.reactionSubscription?.unsubscribe();
    this.messageDeleteSubscription?.unsubscribe();
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    // Close reaction picker when clicking outside
    const target = event.target as HTMLElement;
    if (!target.closest('.reaction-picker') && !target.closest('.btn-add-reaction')) {
      this.showReactionPicker.set(null);
    }
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
        console.log('Message type:', message.messageType, 'Image URL:', message.imageUrl);
        
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

  subscribeToReactions(): void {
    this.reactionSubscription = this.webSocketService.reactions$.subscribe({
      next: (reactionResponse: ReactionResponse) => {
        console.log('Received reaction update:', reactionResponse);
        
        // Update reactions in the message
        this.messages.update(msgs => 
          msgs.map(msg => 
            msg.id === reactionResponse.messageId 
              ? { ...msg, reactions: reactionResponse.reactionCounts }
              : msg
          )
        );
      }
    });
  }

  subscribeToMessageDeletes(): void {
    this.messageDeleteSubscription = this.webSocketService.messageDeletes$.subscribe({
      next: (deleteData: { messageId: number, deletedBy: number }) => {
        console.log('Received message delete notification:', deleteData);
        
        // Remove deleted message from UI
        this.messages.update(msgs => 
          msgs.filter(msg => msg.id !== deleteData.messageId)
        );
      }
    });
  }

  deleteMessage(messageId: number): void {
    if (confirm('Bạn có chắc chắn muốn xóa tin nhắn này? Cả bạn và người nhận đều sẽ không thấy tin nhắn này nữa.')) {
      console.log('Deleting message:', messageId);
      this.webSocketService.deleteMessage(messageId);
    }
  }

  toggleReaction(messageId: number, emoji: string): void {
    console.log('Toggle reaction:', messageId, emoji);
    this.webSocketService.toggleReaction(messageId, emoji);
    this.showReactionPicker.set(null); // Close picker after selection
  }

  toggleReactionPicker(messageId: number, event: Event): void {
    event.stopPropagation();
    if (this.showReactionPicker() === messageId) {
      this.showReactionPicker.set(null);
    } else {
      this.showReactionPicker.set(messageId);
    }
  }

  getReactionEntries(reactions: { [emoji: string]: number } | undefined): [string, number][] {
    if (!reactions) return [];
    return Object.entries(reactions);
  }

  sendMessage(): void {
    // If there's an image, send image message
    if (this.selectedImage()) {
      this.sendImageMessage();
      return;
    }

    // Send text message
    const content = this.messageInput().trim();
    if (!content || this.isSending()) return;

    this.isSending.set(true);
    
    console.log('Sending message:', content);
    
    // Clear input immediately for better UX
    this.messageInput.set('');
    
    // Send via WebSocket
    this.webSocketService.sendMessage(this.selectedUser.id, content, 'TEXT');
    
    // Reset sending state after a short delay
    setTimeout(() => {
      this.isSending.set(false);
    }, 500);
  }

  sendImageMessage(): void {
    const file = this.selectedImage();
    if (!file || this.isUploading()) return;

    this.isUploading.set(true);

    // Upload image first
    this.fileUploadService.uploadImage(file).subscribe({
      next: (response) => {
        console.log('Image uploaded:', response);
        
        if ((response.result === 'SUCCESS' || response.success) && response.data) {
          const imageUrl = response.data.imageUrl;
          const caption = this.messageInput().trim();
          
          // Send image message via WebSocket
          this.webSocketService.sendMessage(
            this.selectedUser.id, 
            caption || 'Đã gửi một ảnh',
            'IMAGE',
            imageUrl
          );

          // Clear input and image
          this.messageInput.set('');
          this.clearSelectedImage();
        }
        
        this.isUploading.set(false);
      },
      error: (error) => {
        console.error('Error uploading image:', error);
        alert('Lỗi khi upload ảnh. Vui lòng thử lại.');
        this.isUploading.set(false);
      }
    });
  }

  onImageSelect(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    
    if (file) {
      // Validate file type
      if (!file.type.startsWith('image/')) {
        alert('Vui lòng chọn file ảnh');
        return;
      }

      // Validate file size (max 5MB)
      if (file.size > 5 * 1024 * 1024) {
        alert('Kích thước ảnh không được vượt quá 5MB');
        return;
      }

      this.selectedImage.set(file);

      // Create preview
      const reader = new FileReader();
      reader.onload = (e) => {
        this.imagePreviewUrl.set(e.target?.result as string);
      };
      reader.readAsDataURL(file);
    }
  }

  clearSelectedImage(): void {
    this.selectedImage.set(null);
    this.imagePreviewUrl.set(null);
    
    // Reset file input
    const fileInput = document.getElementById('imageInput') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  triggerImageSelect(): void {
    const fileInput = document.getElementById('imageInput') as HTMLInputElement;
    fileInput?.click();
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

  isUserOnline(): boolean {
    return this.userStatusMap.get(this.selectedUser.id) === 'ONLINE';
  }

  getUserStatus(): string {
    return this.isUserOnline() ? 'Đang hoạt động' : 'Không hoạt động';
  }

  onBackToList(): void {
    this.backToList.emit();
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
