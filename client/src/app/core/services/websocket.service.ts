import { Injectable } from '@angular/core';
import { Client, Message as StompMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { AuthService } from './auth.service';
import { Message, UserStatus, TypingNotification } from '../models';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private readonly WS_URL = 'http://localhost:8083/ws';
  private stompClient: Client | null = null;
  private connectionStatus = new BehaviorSubject<boolean>(false);
  
  // Subjects for real-time events
  private messageSubject = new Subject<Message>();
  private userStatusSubject = new Subject<UserStatus>();
  private typingSubject = new Subject<TypingNotification>();

  public isConnected$ = this.connectionStatus.asObservable();
  public messages$ = this.messageSubject.asObservable();
  public userStatus$ = this.userStatusSubject.asObservable();
  public typing$ = this.typingSubject.asObservable();

  constructor(private authService: AuthService) {}

  /**
   * Kết nối WebSocket với JWT authentication
   */
  connect(): void {
    const token = this.authService.getToken();
    if (!token) {
      console.error('No token available for WebSocket connection');
      return;
    }

    this.stompClient = new Client({
      webSocketFactory: () => new SockJS(this.WS_URL),
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },
      debug: (str) => {
        console.log('STOMP: ' + str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.stompClient.onConnect = () => {
      console.log('WebSocket connected');
      this.connectionStatus.next(true);
      this.subscribeToTopics();
    };

    this.stompClient.onStompError = (frame) => {
      console.error('STOMP error:', frame);
      this.connectionStatus.next(false);
    };

    this.stompClient.onWebSocketClose = () => {
      console.log('WebSocket disconnected');
      this.connectionStatus.next(false);
    };

    this.stompClient.activate();
  }

  /**
   * Subscribe to WebSocket topics
   */
  private subscribeToTopics(): void {
    if (!this.stompClient) return;

    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return;

    // Subscribe to personal message queue
    this.stompClient.subscribe(`/user/queue/messages`, (message: StompMessage) => {
      const messageData: Message = JSON.parse(message.body);
      this.messageSubject.next(messageData);
    });

    // Subscribe to user status updates
    this.stompClient.subscribe('/topic/users/status', (message: StompMessage) => {
      const statusData: UserStatus = JSON.parse(message.body);
      this.userStatusSubject.next(statusData);
    });

    // Subscribe to typing notifications
    this.stompClient.subscribe('/topic/typing', (message: StompMessage) => {
      const typingData: TypingNotification = JSON.parse(message.body);
      this.typingSubject.next(typingData);
    });
  }

  /**
   * Gửi tin nhắn qua WebSocket
   */
  sendMessage(receiverId: number, content: string, messageType: string = 'TEXT', imageUrl?: string): void {
    if (!this.stompClient || !this.connectionStatus.value) {
      console.error('WebSocket not connected');
      return;
    }

    this.stompClient.publish({
      destination: '/app/chat.sendMessage',
      body: JSON.stringify({
        receiverId: receiverId,
        content: content,
        messageType: messageType,
        imageUrl: imageUrl
      })
    });
  }

  /**
   * Gửi typing notification
   */
  sendTypingNotification(receiverId: number, isTyping: boolean): void {
    if (!this.stompClient || !this.connectionStatus.value) {
      return;
    }

    this.stompClient.publish({
      destination: '/app/chat.typing',
      body: JSON.stringify({
        receiverId: receiverId,
        isTyping: isTyping
      })
    });
  }

  /**
   * Ngắt kết nối WebSocket
   */
  disconnect(): void {
    if (this.stompClient) {
      this.stompClient.deactivate();
      this.connectionStatus.next(false);
    }
  }
}
