# 📚 Hướng Dẫn API - Real Time Chat Application

## 🔧 Cấu Hình Server
- **Port**: 8083
- **Base URL**: `http://localhost:8083`
- **Database**: PostgreSQL (localhost:5432/chatdb)

## 🔐 Authentication
Ứng dụng sử dụng JWT Authentication. Sau khi đăng nhập thành công, bạn sẽ nhận được token và cần thêm vào header:
```
Authorization: Bearer <your-jwt-token>
```

---

## 📋 DANH SÁCH TẤT CẢ API ENDPOINTS

### 🧪 1. TEST ENDPOINTS

#### 1.1 Test Public Endpoint
- **Method**: `GET`
- **URL**: `/api/test/public`
- **Auth**: Không cần
- **Description**: Endpoint test không cần authentication

**Response**:
```json
{
    "result": "SUCCESS",
    "message": "Đây là endpoint public, không cần authentication",
    "data": "Hello World!"
}
```

#### 1.2 Test Protected Endpoint  
- **Method**: `GET`
- **URL**: `/api/test/protected`
- **Auth**: Cần JWT token
- **Description**: Endpoint test cần authentication

**Response**:
```json
{
    "result": "SUCCESS", 
    "message": "Đây là endpoint protected, cần authentication",
    "data": "Hello Authenticated User!"
}
```

---

### 🔑 2. AUTHENTICATION ENDPOINTS

#### 2.1 Đăng Ký User Mới
- **Method**: `POST`
- **URL**: `/api/auth/register`
- **Auth**: Không cần
- **Content-Type**: `application/json`

**Request Body**:
```json
{
    "email": "user@example.com",
    "password": "password123",
    "displayName": "Tên Hiển Thị"
}
```

**Response Success (201)**:
```json
{
    "result": "SUCCESS",
    "message": "Đăng ký thành công",
    "data": {
        "id": 1,
        "email": "user@example.com",
        "displayName": "Tên Hiển Thị",
        "createdAt": "2025-11-04T10:30:00",
        "lastSeen": null
    }
}
```

**Response Error (400)**:
```json
{
    "result": "ERROR",
    "message": "Email đã tồn tại",
    "data": null
}
```

#### 2.2 Đăng Nhập
- **Method**: `POST`
- **URL**: `/api/auth/login`
- **Auth**: Không cần
- **Content-Type**: `application/json`

**Request Body**:
```json
{
    "email": "user@example.com",
    "password": "password123"
}
```

**Response Success (200)**:
```json
{
    "result": "SUCCESS",
    "message": "Đăng nhập thành công",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "user": {
            "id": 1,
            "email": "user@example.com",
            "displayName": "Tên Hiển Thị",
            "createdAt": "2025-11-04T10:30:00",
            "lastSeen": "2025-11-04T10:35:00"
        }
    }
}
```

**Response Error (401)**:
```json
{
    "result": "ERROR",
    "message": "Email hoặc mật khẩu không đúng",
    "data": null
}
```

#### 2.3 Lấy Thông Tin User Hiện Tại
- **Method**: `GET`
- **URL**: `/api/auth/me`
- **Auth**: Cần JWT token
- **Headers**: `Authorization: Bearer <token>`

**Response Success (200)**:
```json
{
    "result": "SUCCESS",
    "message": "Lấy thông tin thành công",
    "data": {
        "id": 1,
        "email": "user@example.com",
        "displayName": "Tên Hiển Thị",
        "createdAt": "2025-11-04T10:30:00",
        "lastSeen": "2025-11-04T10:35:00"
    }
}
```

#### 2.4 Đăng Xuất
- **Method**: `POST`
- **URL**: `/api/auth/logout`
- **Auth**: Cần JWT token
- **Headers**: `Authorization: Bearer <token>`

**Response Success (200)**:
```json
{
    "result": "SUCCESS",
    "message": "Đăng xuất thành công",
    "data": null
}
```

---

### 👥 3. USER MANAGEMENT ENDPOINTS

#### 3.1 Lấy Danh Sách Tất Cả Users
- **Method**: `GET`
- **URL**: `/api/users`
- **Auth**: Cần JWT token
- **Headers**: `Authorization: Bearer <token>`

**Response Success (200)**:
```json
{
    "result": "SUCCESS",
    "message": "Lấy danh sách users thành công",
    "data": [
        {
            "id": 1,
            "email": "user1@example.com",
            "displayName": "User One",
            "createdAt": "2025-11-04T10:30:00",
            "lastSeen": "2025-11-04T10:35:00"
        },
        {
            "id": 2,
            "email": "user2@example.com",
            "displayName": "User Two",
            "createdAt": "2025-11-04T11:00:00",
            "lastSeen": "2025-11-04T11:05:00"
        }
    ]
}
```

#### 3.2 Lấy Thông Tin User Theo ID
- **Method**: `GET`
- **URL**: `/api/users/{userId}`
- **Auth**: Cần JWT token
- **Headers**: `Authorization: Bearer <token>`
- **Path Parameters**: `userId` (Long)

**Example**: `GET /api/users/1`

**Response Success (200)**:
```json
{
    "result": "SUCCESS",
    "message": "Lấy thông tin user thành công",
    "data": {
        "id": 1,
        "email": "user1@example.com",
        "displayName": "User One",
        "createdAt": "2025-11-04T10:30:00",
        "lastSeen": "2025-11-04T10:35:00"
    }
}
```

#### 3.3 Tìm Kiếm Users Theo Tên
- **Method**: `GET`
- **URL**: `/api/users/search?q={query}`
- **Auth**: Cần JWT token
- **Headers**: `Authorization: Bearer <token>`
- **Query Parameters**: `q` (String) - từ khóa tìm kiếm

**Example**: `GET /api/users/search?q=john`

**Response Success (200)**:
```json
{
    "result": "SUCCESS",
    "message": "Tìm kiếm users thành công",
    "data": [
        {
            "id": 3,
            "email": "john@example.com",
            "displayName": "John Doe",
            "createdAt": "2025-11-04T12:00:00",
            "lastSeen": "2025-11-04T12:05:00"
        }
    ]
}
```

---

### 💬 4. MESSAGE ENDPOINTS

#### 4.1 Lấy Tất Cả Conversations
- **Method**: `GET`
- **URL**: `/api/messages`
- **Auth**: Cần JWT token
- **Headers**: `Authorization: Bearer <token>`
- **Description**: Lấy tổng quan tất cả conversations của user hiện tại

**Example**: `GET /api/messages`

**Response Success (200)** - Direct array theo design:
```json
[
    {
        "id": 3,
        "senderId": 2,
        "receiverId": 1,
        "content": "Tin nhắn chưa đọc",
        "sentAt": "2025-11-04T15:00:00",
        "isRead": false,
        "senderDisplayName": "User Two"
    }
]
```

#### 4.2 Lấy Lịch Sử Chat Với User Cụ Thể
- **Method**: `GET`
- **URL**: `/api/messages/{userId}`
- **Auth**: Cần JWT token
- **Headers**: `Authorization: Bearer <token>`
- **Path Parameters**: `userId` (Long) - ID của user muốn xem lịch sử chat

**Example**: `GET /api/messages/2`

    {
        "id": 1,
        "senderId": 1,
        "receiverId": 2,
        "content": "Xin chào!",
        "sentAt": "2025-11-04T14:30:00",
        "isRead": true,
        "senderDisplayName": "User One"
    },
    {
        "id": 2,
        "senderId": 2,
        "receiverId": 1,
        "content": "Chào bạn!",
        "sentAt": "2025-11-04T14:31:00",
        "isRead": false,
        "senderDisplayName": "User Two"
    }
]
```

#### 4.3 Lấy Danh Sách Tin Nhắn Chưa Đọc
- **Method**: `GET`
- **URL**: `/api/messages/unread`
- **Auth**: Cần JWT token
- **Headers**: `Authorization: Bearer <token>`

**Response Success (200)** - Direct array theo design:
```json
[
    {
        "id": 3,
        "senderId": 3,
        "receiverId": 1,
        "content": "Tin nhắn chưa đọc",
        "sentAt": "2025-11-04T15:00:00",
        "isRead": false,
        "senderDisplayName": "User Three"
    }
]
```

#### 4.4 Đánh Dấu Tin Nhắn Đã Đọc
- **Method**: `PUT`
- **URL**: `/api/messages/{messageId}/read`
- **Auth**: Cần JWT token
- **Headers**: `Authorization: Bearer <token>`
- **Path Parameters**: `messageId` (Long)
- **Security**: Chỉ receiver mới có thể đánh dấu tin nhắn đã đọc

**Example**: `PUT /api/messages/3/read`

**Response Success (200)** - Empty body theo design:
```
HTTP 200 OK
(No body)
```

**Response Error (400)**:
```
HTTP 400 Bad Request
(Không có quyền đánh dấu tin nhắn này)
```
```

#### 4.5 Đánh Dấu Tất Cả Tin Nhắn Từ User Đã Đọc
- **Method**: `PUT`
- **URL**: `/api/messages/read-all/{senderId}`
- **Auth**: Cần JWT token
- **Headers**: `Authorization: Bearer <token>`
- **Path Parameters**: `senderId` (Long) - ID của user gửi tin nhắn
- **Security**: Chỉ có thể đánh dấu tin nhắn từ user khác (không phải chính mình)

**Example**: `PUT /api/messages/read-all/2`

**Response Success (200)** - Empty body theo design:
```
HTTP 200 OK
(No body)
```

**Response Error (400)**:
```
HTTP 400 Bad Request
(Không thể đánh dấu tin nhắn của chính mình)
```
```

---

## 🔌 WEBSOCKET ENDPOINTS

### WebSocket Connection
- **URL**: `ws://localhost:8083/ws`
- **Protocol**: STOMP
- **Auth**: Cần JWT token trong header khi connect

### WebSocket Destinations

#### 1. Gửi Tin Nhắn
- **Destination**: `/app/chat.sendMessage`
- **Payload**:
```json
{
    "receiverId": 2,
    "content": "Nội dung tin nhắn"
}
```

#### 2. Thông Báo Typing
- **Destination**: `/app/chat.typing`
- **Payload**:
```json
{
    "receiverId": 2,
    "isTyping": true
}
```

### WebSocket Subscriptions

#### 1. Nhận Tin Nhắn
- **Subscribe**: `/user/queue/messages`
- **Receive**:
```json
{
    "id": 1,
    "senderId": 2,
    "receiverId": 1,
    "content": "Tin nhắn mới",
    "sentAt": "2025-11-04T16:00:00",
    "isRead": false,
    "senderDisplayName": "Sender Name"
}
```

#### 2. Nhận Thông Báo Typing
- **Subscribe**: `/user/queue/typing`
- **Receive**:
```json
{
    "senderId": 2,
    "senderDisplayName": "Sender Name",
    "isTyping": true
}
```

#### 3. Nhận Thông Báo Trạng Thái User
- **Subscribe**: `/topic/users/status`
- **Receive**:
```json
{
    "userId": 2,
    "displayName": "User Name",
    "status": "ONLINE"
}
```

#### 4. Nhận Lỗi
- **Subscribe**: `/user/queue/errors`
- **Receive**: String message lỗi

---

## 🚨 Error Handling

Tất cả lỗi được xử lý qua GlobalExceptionHandler và trả về format:

```json
{
    "result": "ERROR",
    "message": "Mô tả lỗi",
    "data": null
}
```

### Các HTTP Status Codes thường gặp:
- **200**: Success
- **201**: Created (đăng ký thành công)
- **400**: Bad Request (dữ liệu đầu vào không hợp lệ)
- **401**: Unauthorized (chưa đăng nhập hoặc token không hợp lệ)
- **404**: Not Found (resource không tồn tại)
- **500**: Internal Server Error

---

## 🧪 Testing Guidelines

### 1. Thứ tự test API:
1. Test public endpoint (`/api/test/public`)
2. Đăng ký user mới (`/api/auth/register`)
3. Đăng nhập (`/api/auth/login`) → lưu token
4. Test protected endpoint với token (`/api/test/protected`)
5. Test các API khác với token

### 2. JWT Token:
- Lưu token từ response đăng nhập
- Thêm vào header: `Authorization: Bearer <token>`
- Token có thời hạn 24 giờ (86400000ms)

### 3. Database:
- Database sẽ được recreate mỗi lần restart server (`spring.jpa.hibernate.ddl-auto=create-drop`)
- Cần đăng ký user mới sau mỗi lần restart

---

## 📊 Sample Data

Để test, bạn có thể tạo một số user mẫu:

**User 1**:
```json
{
    "email": "alice@example.com",
    "password": "password123",
    "displayName": "Alice Johnson"
}
```

**User 2**:
```json
{
    "email": "bob@example.com", 
    "password": "password123",
    "displayName": "Bob Smith"
}
```

**User 3**:
```json
{
    "email": "charlie@example.com",
    "password": "password123", 
    "displayName": "Charlie Brown"
}
```
