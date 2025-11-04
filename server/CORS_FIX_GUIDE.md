# 🔧 Khắc Phục Lỗi "Failed to fetch" - CORS Issue

## 🚨 **Nguyên Nhân:**
Lỗi này xảy ra vì:
1. **CORS chưa được config đúng** cho file:// protocol
2. **Server cần restart** để apply CORS changes
3. **Browser security** block requests từ file:// tới http://

## ✅ **Giải Pháp:**

### **Cách 1: Restart Server (Khuyến Nghị)**
```bash
# Dừng server hiện tại (Ctrl+C)
# Chạy lại server
cd D:\Code\Projects\RealTimeChat\server
./mvnw spring-boot:run
```

**Đợi thông báo:** `Started ServerApplication in X seconds`

### **Cách 2: Serve HTML từ HTTP Server**
```bash
# Option A: Python HTTP Server (nếu có Python)
cd D:\Code\Projects\RealTimeChat\server
python -m http.server 3000

# Mở browser: http://localhost:3000/websocket-test-client.html
```

```bash
# Option B: Node.js HTTP Server (nếu có Node.js)
cd D:\Code\Projects\RealTimeChat\server
npx http-server -p 3000 --cors

# Mở browser: http://localhost:3000/websocket-test-client.html
```

### **Cách 3: Dùng Live Server (VS Code)**
1. Install extension "Live Server" trong VS Code
2. Right-click file `websocket-test-client.html`
3. Chọn "Open with Live Server"
4. Sẽ mở tại `http://127.0.0.1:5500/websocket-test-client.html`

## 🔍 **Kiểm Tra Sau Khi Restart:**

### Bước 1: Test Server
1. Mở HTML client
2. Click **"🔧 Test Server"**
3. Expect: `✅ Server hoạt động tốt`

### Bước 2: Test Token
1. Lấy JWT token từ Postman:
```json
POST http://localhost:8083/api/auth/login
{
    "email": "test@gmail.com",
    "password": "123456"
}
```
2. Copy token (KHÔNG có "Bearer ")
3. Paste vào HTML client
4. Click **"🔍 Check Token"**
5. Expect: `✅ Token hợp lệ cho user: ...`

### Bước 3: Test WebSocket
1. Click **"🔌 Connect"**
2. Expect: `✅ Connected`
3. Gửi tin nhắn test

## 🛠️ **Nếu Vẫn Lỗi:**

### Check CORS Config:
```java
// File: SecurityConfig.java
configuration.setAllowedOriginPatterns(Arrays.asList(
    "http://localhost:*", 
    "http://127.0.0.1:*",
    "file://*"  // ← Đã thêm dòng này
));
```

### Check WebSocket Config:
```java
// File: WebSocketConfig.java
registry.addEndpoint("/ws")
    .setAllowedOriginPatterns("*") // ← Đã có
    .withSockJS();
```

### Check Server Logs:
```bash
# Khi test server, expect thấy:
DEBUG o.s.security.web.FilterChainProxy - Securing GET /api/test/public
```

## 📊 **Test với Postman (Backup Plan):**

Nếu HTML client không work, dùng Postman:

### Test REST APIs:
```bash
# Test public endpoint
GET http://localhost:8083/api/test/public

# Test with token  
GET http://localhost:8083/api/auth/me
Authorization: Bearer <your-token>
```

### Test WebSocket (Postman Beta):
1. New → WebSocket Request
2. URL: `ws://localhost:8083/ws`
3. Headers: `Authorization: Bearer <token>`
4. Connect → Send STOMP message

## 🎯 **Expected Timeline:**
- **Restart server**: ~30 giây
- **Test server connectivity**: ~5 giây  
- **WebSocket connection**: ~2 giây
- **Send first message**: Instant

**🚀 Hãy restart server trước, rồi test lại HTML client!**
