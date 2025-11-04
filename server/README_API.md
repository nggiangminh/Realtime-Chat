# 📱 Real Time Chat Application - API Documentation

> **Ứng dụng chat thời gian thực** được xây dựng với Spring Boot 3, WebSocket và JWT Authentication

## 🚀 Quick Start

### 1. Khởi động ứng dụng
```bash
cd D:\Code\Projects\RealTimeChat\server
./mvnw spring-boot:run
```
Server sẽ chạy trên: **http://localhost:8083**

### 2. Test API với Postman
1. Import file `Real_Time_Chat_API.postman_collection.json` vào Postman
2. Tạo environment với `baseUrl` = `http://localhost:8083`
3. Đăng ký user → Đăng nhập → Test các API

---

## 📁 Tài Liệu API

| File | Mô tả |
|------|-------|
| [**API_GUIDE.md**](./API_GUIDE.md) | 📚 Hướng dẫn chi tiết tất cả API endpoints |
| [**POSTMAN_GUIDE.md**](./POSTMAN_GUIDE.md) | 🚀 Hướng dẫn sử dụng Postman để test API |
| [**Real_Time_Chat_API.postman_collection.json**](./Real_Time_Chat_API.postman_collection.json) | 📦 Postman Collection có sẵn |

---

## 🔧 Cấu Hình Hệ Thống

### Tech Stack
- **Framework**: Spring Boot 3
- **Java**: JDK 17
- **Database**: PostgreSQL
- **Authentication**: JWT
- **Real-time**: WebSocket + STOMP
- **Build Tool**: Maven

### Database Configuration
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/chatdb
spring.datasource.username=postgres
spring.datasource.password=Minh2263@
```

### JWT Configuration
```properties
app.jwt.secret=mySecretKey123456789012345678901234567890abcdefghijklmnopqrstuvwxyz
app.jwt.expiration=86400000  # 24 hours
```

---

## 🗂️ Cấu Trúc API

### 🧪 Test Endpoints
| Method | URL | Auth | Description |
|--------|-----|------|-------------|
| GET | `/api/test/public` | ❌ | Test endpoint công khai |
| GET | `/api/test/protected` | ✅ | Test endpoint bảo mật |

### 🔑 Authentication  
| Method | URL | Auth | Description |
|--------|-----|------|-------------|
| POST | `/api/auth/register` | ❌ | Đăng ký user mới |
| POST | `/api/auth/login` | ❌ | Đăng nhập |
| GET | `/api/auth/me` | ✅ | Lấy thông tin user hiện tại |
| POST | `/api/auth/logout` | ✅ | Đăng xuất |

### 👥 User Management
| Method | URL | Auth | Description |
|--------|-----|------|-------------|
| GET | `/api/users` | ✅ | Lấy danh sách tất cả users |
| GET | `/api/users/{id}` | ✅ | Lấy thông tin user theo ID |
| GET | `/api/users/search?q={query}` | ✅ | Tìm kiếm users theo tên |

### 💬 Messages
| Method | URL | Auth | Description |
|--------|-----|------|-------------|
| GET | `/api/messages/{userId}` | ✅ | Lấy lịch sử chat với user |
| GET | `/api/messages/unread` | ✅ | Lấy tin nhắn chưa đọc |
| PUT | `/api/messages/{messageId}/read` | ✅ | Đánh dấu tin nhắn đã đọc |
| PUT | `/api/messages/read-all/{senderId}` | ✅ | Đánh dấu tất cả tin nhắn đã đọc |

### 🔌 WebSocket Endpoints
| Type | Destination | Description |
|------|-------------|-------------|
| **Send** | `/app/chat.sendMessage` | Gửi tin nhắn |
| **Send** | `/app/chat.typing` | Thông báo typing |
| **Subscribe** | `/user/queue/messages` | Nhận tin nhắn |
| **Subscribe** | `/user/queue/typing` | Nhận thông báo typing |
| **Subscribe** | `/topic/users/status` | Nhận trạng thái user |

---

## 🔐 Authentication Flow

### 1. Đăng ký
```bash
POST /api/auth/register
{
    "email": "user@example.com",
    "password": "password123",
    "displayName": "Display Name"
}
```

### 2. Đăng nhập  
```bash
POST /api/auth/login
{
    "email": "user@example.com", 
    "password": "password123"
}
```

**Response:**
```json
{
    "result": "SUCCESS",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "user": { ... }
    }
}
```

### 3. Sử dụng token
```bash
Authorization: Bearer <your-jwt-token>
```

---

## 🧪 Sample Test Flow

### Bước 1: Tạo Users
```bash
# User 1
POST /api/auth/register
{
    "email": "alice@example.com",
    "password": "password123", 
    "displayName": "Alice Johnson"
}

# User 2
POST /api/auth/register
{
    "email": "bob@example.com",
    "password": "password123",
    "displayName": "Bob Smith"
}
```

### Bước 2: Đăng nhập
```bash
POST /api/auth/login
{
    "email": "alice@example.com",
    "password": "password123"
}
# → Lưu token từ response
```

### Bước 3: Test APIs
```bash
# Test với token
GET /api/users
Authorization: Bearer <token>

GET /api/users/search?q=bob
Authorization: Bearer <token>

GET /api/messages/2
Authorization: Bearer <token>
```

---

## 🌐 WebSocket Connection

### JavaScript Client Example
```javascript
// Kết nối
const socket = new SockJS('http://localhost:8083/ws');
const stompClient = Stomp.over(socket);

// Đăng nhập với token
const headers = {
    'Authorization': 'Bearer ' + jwtToken
};

stompClient.connect(headers, function(frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe nhận tin nhắn
    stompClient.subscribe('/user/queue/messages', function(message) {
        const messageData = JSON.parse(message.body);
        console.log('New message:', messageData);
    });
    
    // Gửi tin nhắn
    stompClient.send('/app/chat.sendMessage', {}, JSON.stringify({
        'receiverId': 2,
        'content': 'Hello!'
    }));
});
```

---

## 🚨 Error Handling

### Response Format
```json
{
    "result": "SUCCESS" | "ERROR",
    "message": "Mô tả kết quả",
    "data": <actual_data> | null
}
```

### Common Status Codes
- **200**: OK
- **201**: Created (đăng ký thành công)
- **400**: Bad Request (dữ liệu không hợp lệ)
- **401**: Unauthorized (chưa đăng nhập/token hết hạn)
- **404**: Not Found
- **500**: Internal Server Error

---

## 🔧 Development Setup

### Database Setup
```sql
-- Tạo database
CREATE DATABASE chatdb;

-- Cấp quyền cho user postgres
GRANT ALL PRIVILEGES ON DATABASE chatdb TO postgres;
```

### Run Application
```bash
# Clone project
git clone <repository-url>
cd server

# Run với Maven
./mvnw spring-boot:run

# Hoặc build và run
./mvnw clean package
java -jar target/server-0.0.1-SNAPSHOT.jar
```

### Environment Variables
```bash
# Database config
DB_URL=jdbc:postgresql://localhost:5432/chatdb
DB_USERNAME=postgres  
DB_PASSWORD=Minh2263@

# JWT config
JWT_SECRET=mySecretKey123456789012345678901234567890abcdefghijklmnopqrstuvwxyz
JWT_EXPIRATION=86400000
```

---

## 📊 Kiến Trúc Dự Án

```
📦 server/
├── 📁 src/main/java/com/learning/server/
│   ├── 📁 common/          # ApiResponse class
│   ├── 📁 config/          # Security, WebSocket config  
│   ├── 📁 controller/      # REST Controllers
│   ├── 📁 dto/            # Data Transfer Objects
│   ├── 📁 entity/         # JPA Entities
│   ├── 📁 exception/      # Global Exception Handler
│   ├── 📁 repository/     # JPA Repositories
│   ├── 📁 security/       # JWT Security classes
│   └── 📁 service/        # Business Logic Services
├── 📁 src/main/resources/
│   └── application.properties
├── 📄 API_GUIDE.md
├── 📄 POSTMAN_GUIDE.md
├── 📄 Real_Time_Chat_API.postman_collection.json
└── 📄 README_API.md (this file)
```

---

## 🔍 Troubleshooting

### ❌ Server không khởi động được
1. Kiểm tra PostgreSQL đã chạy chưa
2. Kiểm tra database `chatdb` đã tồn tại chưa
3. Kiểm tra port 8083 có bị chiếm không

### ❌ API trả về 401 Unauthorized
1. Kiểm tra đã đăng nhập và có token chưa
2. Kiểm tra token có được thêm vào header đúng format không
3. Kiểm tra token có hết hạn không (24h)

### ❌ WebSocket không kết nối được
1. Kiểm tra server đã chạy chưa
2. Kiểm tra JWT token có hợp lệ không
3. Kiểm tra WebSocket URL: `ws://localhost:8083/ws`

---

## 📞 Support

Nếu gặp vấn đề, hãy kiểm tra:

1. **Logs**: Xem console output khi chạy server
2. **Database**: Kiểm tra kết nối PostgreSQL
3. **Postman**: Kiểm tra environment variables và token
4. **Browser DevTools**: Kiểm tra WebSocket connection

---

**🎉 Happy Coding! Chúc bạn test API thành công!** 

*Cập nhật lần cuối: November 4, 2025*
