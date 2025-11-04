# 🚀 Quick Start - Test WebSocket Message API

## ⚡ Bắt Đầu Nhanh (5 phút)

### 1. 🔧 Khởi động Server
```bash
cd D:\Code\Projects\RealTimeChat\server
./mvnw spring-boot:run
```
**Đợi thông báo:** `Started ServerApplication in X seconds`

### 2. 📱 Tạo Users Test
**Mở Postman → Import collection → Chạy theo thứ tự:**

1. **Đăng ký Alice:**
```json
POST /api/auth/register
{
    "email": "alice@test.com",
    "password": "password123",
    "displayName": "Alice"
}
```

2. **Đăng ký Bob:**
```json
POST /api/auth/register  
{
    "email": "bob@test.com", 
    "password": "password123",
    "displayName": "Bob"
}
```

3. **Đăng nhập Alice:**
```json
POST /api/auth/login
{
    "email": "alice@test.com",
    "password": "password123"
}
```
**→ Copy `token` từ response**

### 3. 🔌 Test WebSocket

#### Cách 1: HTML Test Client (Dễ nhất)
1. Mở file: `websocket-test-client.html` trong browser
2. Paste JWT token vào ô input
3. Click "🔌 Connect" 
4. Nhập "Receiver ID: 2" (Bob)
5. Nhập tin nhắn và click "📤 Send"

#### Cách 2: JavaScript Console
```javascript
// Paste vào Browser Console (F12)
const socket = new SockJS('http://localhost:8083/ws');
const client = Stomp.over(socket);

client.connect({'Authorization': 'Bearer YOUR_TOKEN_HERE'}, function(frame) {
    console.log('Connected:', frame);
    
    // Subscribe nhận tin nhắn
    client.subscribe('/user/queue/messages', function(msg) {
        console.log('Received:', JSON.parse(msg.body));
    });
    
    // Gửi tin nhắn
    client.send('/app/chat.sendMessage', {}, JSON.stringify({
        receiverId: 2,
        content: 'Hello Bob!'
    }));
});
```

---

## 📋 Test Scenarios Cơ Bản

### ✅ Scenario 1: Gửi Tin Nhắn Thành Công
```
1. Alice connect WebSocket ✅
2. Alice gửi "Hello" tới Bob (ID=2) ✅  
3. Tin nhắn xuất hiện trong "Received Messages" ✅
4. Check database có tin nhắn ✅
```

### ✅ Scenario 2: Chat 2 Chiều
```
1. Mở 2 tabs browser ✅
2. Tab 1: Alice connect ✅
3. Tab 2: Bob connect (cần đăng nhập Bob trước) ✅
4. Alice gửi → Bob nhận real-time ✅
5. Bob reply → Alice nhận real-time ✅
```

### ❌ Scenario 3: Error Cases
```
1. Gửi không có token → Connection failed ❌
2. Gửi tới user không tồn tại → Error message ❌
3. Gửi tin nhắn rỗng → Validation error ❌
```

---

## 🔍 Debugging Tips

### Kiểm tra Server Log
```bash
# Khi gửi tin nhắn thành công sẽ thấy:
INFO c.l.s.controller.ChatWebSocketController : Nhận tin nhắn từ user: alice@test.com, tới user: 2
INFO c.l.s.controller.ChatWebSocketController : Đã gửi tin nhắn thành công: 1
```

### Kiểm tra Database
```sql
-- Xem tin nhắn đã lưu
SELECT id, sender_id, receiver_id, content, sent_at 
FROM messages 
ORDER BY sent_at DESC;
```

### Browser Console Errors
```
F12 → Console tab
Tìm lỗi WebSocket connection hoặc STOMP errors
```

---

## 🚨 Troubleshooting

| Vấn đề | Nguyên nhân | Giải pháp |
|---------|-------------|-----------|
| `Connection refused` | Server chưa chạy | Chạy `./mvnw spring-boot:run` |
| `HTTP 403 Forbidden` | Token sai/hết hạn | Đăng nhập lại lấy token mới |
| `User không tồn tại` | Receiver ID sai | Kiểm tra user ID trong database |
| `WebSocket disconnected` | Network issue | Refresh page và connect lại |

---

## 📊 REST API Verification

Sau khi gửi tin nhắn qua WebSocket, verify bằng REST API:

```bash
# Xem lịch sử chat Alice với Bob
GET /api/messages/2
Authorization: Bearer <alice_token>

# Expect response:
{
  "result": "SUCCESS",
  "data": [
    {
      "id": 1,
      "senderId": 1,
      "receiverId": 2, 
      "content": "Hello Bob!",
      "sentAt": "2025-11-04T...",
      "isRead": false,
      "senderDisplayName": "Alice"
    }
  ]
}
```

---

## 🎯 Expected Results

### ✅ Thành Công Khi:
- WebSocket connection status: "✅ Connected"
- Gửi tin nhắn không có lỗi
- Tin nhắn hiển thị trong messages area
- Server log ghi nhận tin nhắn
- Database có record mới
- REST API trả về tin nhắn đã gửi

### ❌ Kiểm tra lại nếu:
- Connection failed  
- Tin nhắn không gửi được
- Không nhận được tin nhắn
- Server báo lỗi 403/401
- Database không có record

---

**⏱️ Thời gian test: ~5-10 phút**  
**🎯 Mục tiêu: Gửi và nhận tin nhắn real-time thành công**

> **💡 Tip:** Bắt đầu với HTML test client trước, sau đó thử các phương pháp khác!
