# RealTime Chat Application - Frontend (Angular)

## 📋 Mô Tả
Frontend của ứng dụng chat realtime được xây dựng với Angular 20, sử dụng WebSocket (STOMP) để giao tiếp realtime với backend Spring Boot.

## 🎨 Design
- **Style**: Flat Design
- **Color Scheme**: 
  - Primary: Purple (#7c3aed, #5b21b6)
  - Background: Black (#1a1a1a), White (#ffffff)
  - Accent: Gray shades

## 🏗️ Cấu Trúc Dự Án

```
src/app/
├── core/                          # Core module - Singleton services
│   ├── guards/                    # Route guards
│   │   └── auth.guard.ts         # Authentication guard
│   ├── interceptors/              # HTTP interceptors
│   │   └── jwt.interceptor.ts    # JWT token interceptor
│   ├── models/                    # TypeScript interfaces
│   │   ├── auth.model.ts         # Auth-related models
│   │   ├── message.model.ts      # Message models
│   │   └── user.model.ts         # User models
│   └── services/                  # Core services
│       ├── auth.service.ts       # Authentication service
│       ├── message.service.ts    # Message API service
│       ├── user.service.ts       # User API service
│       └── websocket.service.ts  # WebSocket STOMP service
│
├── features/                      # Feature modules
│   ├── auth/                     # Authentication feature
│   │   ├── login/                # Login component
│   │   │   ├── login.component.ts
│   │   │   ├── login.component.html
│   │   │   └── login.component.css
│   │   └── register/             # Register component
│   │       ├── register.component.ts
│   │       ├── register.component.html
│   │       └── register.component.css
│   │
│   └── chat/                     # Chat feature
│       ├── chat.component.ts     # Main chat container
│       ├── chat.component.html
│       ├── chat.component.css
│       ├── chat-list/            # User list sidebar
│       │   ├── chat-list.component.ts
│       │   ├── chat-list.component.html
│       │   └── chat-list.component.css
│       └── chat-window/          # Chat messages window
│           ├── chat-window.component.ts
│           ├── chat-window.component.html
│           └── chat-window.component.css
│
├── environments/                  # Environment configs
│   ├── environment.ts            # Development config
│   └── environment.prod.ts       # Production config
│
├── app.config.ts                 # Application configuration
├── app.routes.ts                 # Application routes
├── app.ts                        # Root component
├── app.html                      # Root template
└── app.css                       # Root styles
```

## 🚀 Cài Đặt

### Prerequisites
- Node.js (v18+)
- npm hoặc yarn
- Angular CLI (`npm install -g @angular/cli`)

### Installation
```bash
# Navigate to client folder
cd client

# Install dependencies
npm install

# Start development server
npm start
# hoặc
ng serve

# Truy cập http://localhost:4200
```

## 🔧 Cấu Hình

### API Endpoints
Cấu hình trong `src/environments/environment.ts`:
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8083/api',
  wsUrl: 'http://localhost:8083/ws'
};
```

## 📱 Tính Năng

### ✅ Authentication
- [x] Đăng ký tài khoản mới
- [x] Đăng nhập với email/password
- [x] JWT token authentication
- [x] Auto-redirect với route guards
- [x] Logout

### ✅ Chat Features
- [x] Danh sách users
- [x] Tìm kiếm users
- [x] Real-time messaging qua WebSocket
- [x] Message history
- [x] Online status indicator
- [x] Typing indicator support (backend)
- [x] Sent/Received message differentiation
- [x] Message timestamps

### 🎨 UI/UX
- [x] Flat design với purple theme
- [x] Responsive layout
- [x] Loading states
- [x] Error handling và validation
- [x] Smooth animations
- [x] Clean và modern interface

## 🔐 Security

- JWT token lưu trong localStorage
- HTTP Interceptor tự động thêm Authorization header
- Route guards bảo vệ authenticated routes
- WebSocket authentication với JWT token

## 📦 Dependencies Chính

```json
{
  "@angular/core": "^20.3.0",
  "@angular/common": "^20.3.0",
  "@angular/router": "^20.3.0",
  "@angular/forms": "^20.3.0",
  "@stomp/stompjs": "latest",
  "sockjs-client": "latest",
  "rxjs": "~7.8.0"
}
```

## 🎯 API Integration

### REST APIs
- `POST /api/auth/register` - Đăng ký
- `POST /api/auth/login` - Đăng nhập
- `GET /api/users` - Lấy danh sách users
- `GET /api/messages/:userId` - Lấy lịch sử chat

### WebSocket
- **Connect**: `ws://localhost:8083/ws` (với SockJS fallback)
- **Subscribe**: `/user/queue/messages` - Nhận tin nhắn cá nhân
- **Publish**: `/app/chat.sendMessage` - Gửi tin nhắn

## 🧪 Testing
```bash
# Run unit tests
npm test

# Run e2e tests
npm run e2e
```

## 📦 Build for Production
```bash
# Build production bundle
npm run build

# Output sẽ ở trong dist/client/
```

## 🐛 Troubleshooting

### WebSocket Connection Failed
- Kiểm tra backend đã chạy chưa (port 8083)
- Kiểm tra CORS configuration
- Kiểm tra JWT token hợp lệ

### Can't Login
- Kiểm tra backend API đang chạy
- Kiểm tra email/password đúng
- Xem Network tab trong DevTools

## 📝 Notes

- Port mặc định: 4200 (frontend), 8083 (backend)
- WebSocket sử dụng STOMP protocol với SockJS fallback
- Responsive design support mobile devices
- Flat design với focus vào UX

## 👥 Author
Developed as part of Real-Time Chat Application project

## 📄 License
MIT
