# Realtime Chat Application - Design Document

## 1. Tổng Quan Hệ Thống

### 1.1 Mô Tả
Ứng dụng chat realtime đơn giản cho phép người dùng đăng nhập và trao đổi tin nhắn văn bản theo thời gian thực.

### 1.2 Tech Stack
- **Backend**: Spring Boot 3, WebSocket, Spring Security, JPA/Hibernate
- **Frontend**: Angular, RxJS, WebSocket Client
- **Database**: PostgreSQL/MySQL
- **Protocol**: WebSocket (STOMP)

---

## 2. Kiến Trúc Hệ Thống

### 2.1 Sơ Đồ Tổng Quan
```
┌─────────────┐         WebSocket/HTTP        ┌──────────────┐
│   Angular   │ ◄──────────────────────────► │  Spring Boot │
│   Frontend  │                               │   Backend    │
└─────────────┘                               └──────┬───────┘
                                                     │
                                                     ▼
                                              ┌──────────────┐
                                              │   Database   │
                                              │ (PostgreSQL) │
                                              └──────────────┘
```

### 2.2 Communication Flow
1. **Authentication**: HTTP REST API (POST /api/auth/login)
2. **Real-time Chat**: WebSocket (STOMP over WebSocket)
3. **Message History**: HTTP REST API (GET /api/messages)

---

## 3. Database Design

### 3.1 ERD (Entity Relationship Diagram)
```
┌─────────────────┐          ┌──────────────────┐
│     users       │          │    messages      │
├─────────────────┤          ├──────────────────┤
│ id (PK)         │          │ id (PK)          │
│ email (UNIQUE)  │          │ sender_id (FK)   │
│ password        │          │ receiver_id (FK) │
│ display_name    │          │ content          │
│ created_at      │          │ sent_at          │
│ last_seen       │          │ is_read          │
└─────────────────┘          └──────────────────┘
         │                            │
         └────────────────────────────┘
              1:N relationship
```

### 3.2 Table Definitions

#### Table: users
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | User ID |
| email | VARCHAR(255) | UNIQUE, NOT NULL | Email đăng nhập |
| password | VARCHAR(255) | NOT NULL | Mật khẩu (BCrypt) |
| display_name | VARCHAR(100) | NOT NULL | Tên hiển thị |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Ngày tạo tài khoản |
| last_seen | TIMESTAMP | NULL | Lần online cuối |

#### Table: messages
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Message ID |
| sender_id | BIGINT | FOREIGN KEY (users.id), NOT NULL | Người gửi |
| receiver_id | BIGINT | FOREIGN KEY (users.id), NOT NULL | Người nhận |
| content | TEXT | NOT NULL | Nội dung tin nhắn |
| sent_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Thời gian gửi |
| is_read | BOOLEAN | DEFAULT FALSE | Trạng thái đã đọc |

---

## 4. API Design

### 4.1 REST API Endpoints

#### Authentication APIs
```
POST /api/auth/register
Request Body:
{
  "email": "user@example.com",
  "password": "password123",
  "displayName": "John Doe"
}
Response: 201 Created
{
  "id": 1,
  "email": "user@example.com",
  "displayName": "John Doe"
}
```

```
POST /api/auth/login
Request Body:
{
  "email": "user@example.com",
  "password": "password123"
}
Response: 200 OK
{
  "token": "jwt_token_here",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "displayName": "John Doe"
  }
}
```

#### User APIs
```
GET /api/users
Response: 200 OK
[
  {
    "id": 1,
    "email": "user1@example.com",
    "displayName": "User One",
    "lastSeen": "2025-11-04T10:30:00"
  }
]
```

```
GET /api/users/{userId}
Response: 200 OK
{
  "id": 1,
  "email": "user@example.com",
  "displayName": "John Doe",
  "lastSeen": "2025-11-04T10:30:00"
}
```

#### Message APIs
```
GET /api/messages/{userId}
Description: Lấy lịch sử chat với user có userId
Response: 200 OK
[
  {
    "id": 1,
    "senderId": 1,
    "receiverId": 2,
    "content": "Hello!",
    "sentAt": "2025-11-04T10:00:00",
    "isRead": true
  }
]
```

```
PUT /api/messages/{messageId}/read
Description: Đánh dấu tin nhắn đã đọc
Response: 200 OK
```

### 4.2 WebSocket Protocol (STOMP)

#### Connection
```
Endpoint: ws://localhost:8080/ws
Protocol: STOMP over WebSocket
Authentication: JWT token in header
```

#### Subscribe Topics
```
/user/queue/messages
Description: Nhận tin nhắn cá nhân (private messages)
```

```
/topic/users/status
Description: Nhận thông báo online/offline của users
```

#### Send Destinations
```
/app/chat.sendMessage
Payload:
{
  "receiverId": 2,
  "content": "Hello there!"
}
```

```
/app/chat.typing
Payload:
{
  "receiverId": 2,
  "isTyping": true
}
```

---

## 5. Backend Architecture (Spring Boot)

### 5.1 Package Structure
```
com.example.chatapp
├── config
│   ├── WebSocketConfig.java
│   ├── SecurityConfig.java
│   └── JwtConfig.java
├── controller
│   ├── AuthController.java
│   ├── UserController.java
│   └── MessageController.java
├── websocket
│   └── ChatWebSocketController.java
├── service
│   ├── AuthService.java
│   ├── UserService.java
│   └── MessageService.java
├── repository
│   ├── UserRepository.java
│   └── MessageRepository.java
├── model
│   ├── User.java
│   ├── Message.java
│   └── ChatMessage.java (DTO for WebSocket)
├── security
│   ├── JwtTokenProvider.java
│   └── JwtAuthenticationFilter.java
└── ChatApplication.java
```

### 5.2 Core Components

#### WebSocketConfig
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    // Configure STOMP endpoints
    // Configure message broker
    // Add JWT authentication interceptor
}
```

#### SecurityConfig
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // Configure HTTP security
    // JWT authentication filter
    // CORS configuration
}
```

#### ChatWebSocketController
```java
@Controller
public class ChatWebSocketController {
    @MessageMapping("/chat.sendMessage")
    @SendToUser("/queue/messages")
    public ChatMessage sendMessage(ChatMessage message);
    
    @MessageMapping("/chat.typing")
    public void typing(TypingNotification notification);
}
```

---

## 6. Frontend Architecture (Angular)

### 6.1 Project Structure
```
src/app
├── core
│   ├── services
│   │   ├── auth.service.ts
│   │   ├── websocket.service.ts
│   │   ├── message.service.ts
│   │   └── user.service.ts
│   ├── guards
│   │   └── auth.guard.ts
│   ├── interceptors
│   │   └── jwt.interceptor.ts
│   └── models
│       ├── user.model.ts
│       ├── message.model.ts
│       └── auth.model.ts
├── features
│   ├── auth
│   │   ├── login
│   │   │   ├── login.component.ts
│   │   │   ├── login.component.html
│   │   │   └── login.component.scss
│   │   └── register
│   │       ├── register.component.ts
│   │       ├── register.component.html
│   │       └── register.component.scss
│   └── chat
│       ├── chat-list
│       │   ├── chat-list.component.ts
│       │   ├── chat-list.component.html
│       │   └── chat-list.component.scss
│       ├── chat-window
│       │   ├── chat-window.component.ts
│       │   ├── chat-window.component.html
│       │   └── chat-window.component.scss
│       └── chat.component.ts
└── shared
    ├── components
    └── pipes
```

### 6.2 Core Services

#### AuthService
```typescript
@Injectable({ providedIn: 'root' })
export class AuthService {
  login(email: string, password: string): Observable<AuthResponse>
  register(user: RegisterRequest): Observable<User>
  logout(): void
  getToken(): string | null
  isAuthenticated(): boolean
}
```

#### WebSocketService
```typescript
@Injectable({ providedIn: 'root' })
export class WebSocketService {
  connect(): void
  disconnect(): void
  sendMessage(message: ChatMessage): void
  onMessageReceived(): Observable<Message>
  onUserStatusChanged(): Observable<UserStatus>
}
```

#### MessageService
```typescript
@Injectable({ providedIn: 'root' })
export class MessageService {
  getMessageHistory(userId: number): Observable<Message[]>
  markAsRead(messageId: number): Observable<void>
  sendTypingNotification(userId: number, isTyping: boolean): void
}
```

### 6.3 Component Hierarchy
```
AppComponent
└── ChatComponent (authenticated)
    ├── ChatListComponent
    │   └── UserItemComponent (repeated)
    └── ChatWindowComponent
        ├── MessageListComponent
        │   └── MessageItemComponent (repeated)
        └── MessageInputComponent
```

---

## 7. Security Design

### 7.1 Authentication Flow
1. User gửi email/password tới `/api/auth/login`
2. Backend xác thực và trả về JWT token
3. Frontend lưu token vào localStorage
4. Mọi request sau đó đều gửi kèm token trong header: `Authorization: Bearer <token>`
5. WebSocket connection cũng sử dụng JWT token để authenticate

### 7.2 Password Security
- Sử dụng BCrypt để hash password (strength: 10-12)
- Không bao giờ trả password trong response
- Password tối thiểu 8 ký tự

### 7.3 WebSocket Security
- Validate JWT token khi establish connection
- Kiểm tra quyền truy cập mỗi message
- Chỉ cho phép gửi message cho user khác (không tự gửi cho mình)

### 7.4 CORS Configuration
```
Allowed Origins: http://localhost:4200 (development)
Allowed Methods: GET, POST, PUT, DELETE
Allowed Headers: Authorization, Content-Type
```

---

## 8. UI/UX Design

### 8.1 Screen Flow
```
Login Screen
    ↓
Chat Dashboard
├── Left Panel: User List
│   ├── Search bar
│   ├── Online users (green dot)
│   └── Offline users (grey)
└── Right Panel: Chat Window
    ├── Header: Selected user info
    ├── Messages area (scrollable)
    └── Input box with Send button
```

### 8.2 Main Screens

#### Login Screen
- Email input field
- Password input field (masked)
- Login button
- Link to Register page
- Error message display

#### Chat Dashboard
- **Left Sidebar (30% width)**:
    - Current user profile
    - Search users
    - List of users (online status indicator)
    - Unread message badge

- **Main Chat Area (70% width)**:
    - Chat header: User name, online status, last seen
    - Message list: Sent messages (right-aligned, blue), Received messages (left-aligned, grey)
    - Timestamp for each message
    - Typing indicator
    - Message input box
    - Send button

### 8.3 Responsive Design
- Desktop: Sidebar + Chat window side by side
- Mobile: Toggle between User list và Chat window

---

## 9. Real-time Features

### 9.1 Message Delivery
1. User A gửi message qua WebSocket
2. Backend nhận và lưu vào database
3. Backend forward message tới User B qua WebSocket subscription
4. User B nhận và hiển thị message ngay lập tức

### 9.2 Online Status
- User online: WebSocket connected → broadcast status "ONLINE"
- User offline: WebSocket disconnected → broadcast status "OFFLINE"
- Display green/grey indicator trên user list

### 9.3 Typing Indicator
- User đang gõ → gửi typing notification qua WebSocket
- Hiển thị "User is typing..." trong chat window
- Clear indicator sau 3 giây không có typing event

### 9.4 Read Receipts
- Khi user mở chat với người khác → gọi API mark messages as read
- Display checkmark icon: Single check (sent), Double check (read)

---

## 10. Error Handling

### 10.1 Backend Error Responses
```json
{
  "timestamp": "2025-11-04T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Email already exists",
  "path": "/api/auth/register"
}
```

### 10.2 Common Error Scenarios
- **401 Unauthorized**: Token hết hạn hoặc invalid → Redirect to login
- **403 Forbidden**: Không có quyền truy cập
- **404 Not Found**: User hoặc message không tồn tại
- **500 Internal Server Error**: Lỗi server → Hiển thị generic error message
- **WebSocket Connection Failed**: Retry connection với exponential backoff

---

## 11. Testing Strategy

### 11.1 Backend Testing
- **Unit Tests**: Service layer, Repository layer
- **Integration Tests**: REST API endpoints, WebSocket handlers
- **Security Tests**: Authentication, Authorization

### 11.2 Frontend Testing
- **Unit Tests**: Services, Components (isolated)
- **Integration Tests**: Component interactions
- **E2E Tests**: User flows (login → send message → logout)

---

## 12. Deployment

### 12.1 Development Environment
- Backend: `mvn spring-boot:run` (port 8080)
- Frontend: `ng serve` (port 4200)
- Database: Docker container hoặc local installation

### 12.2 Production Considerations
- **Backend**: JAR file deployment (Docker recommended)
- **Frontend**: Build production (`ng build --prod`) → Deploy to Nginx/Apache
- **Database**: Managed service (AWS RDS, Azure Database)
- **SSL/TLS**: Required for production (WSS for WebSocket)
- **Environment Variables**: Database credentials, JWT secret

---

## 13. Future Enhancements (Out of Scope)

- Group chat
- File/image sharing
- Voice/video call
- Message reactions (emoji)
- Message editing/deletion
- Push notifications
- User profile customization
- Message search
- Encryption (E2E)

---

## 14. Glossary

- **STOMP**: Simple Text Oriented Messaging Protocol
- **JWT**: JSON Web Token
- **BCrypt**: Password hashing algorithm
- **WebSocket**: Full-duplex communication protocol
- **RxJS**: Reactive Extensions for JavaScript

---

## 15. References

- Spring Boot WebSocket Documentation
- Angular WebSocket Integration
- STOMP Protocol Specification
- JWT Best Practices