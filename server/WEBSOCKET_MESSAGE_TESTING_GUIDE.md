# 💬 Hướng Dẫn Test API Gửi Tin Nhắn

## 🔍 Tổng Quan

Trong dự án Real Time Chat này, **gửi tin nhắn** được thực hiện qua **WebSocket** (không phải REST API thông thường). Điều này cho phép tin nhắn được gửi và nhận **thời gian thực**.

### 📋 Các Phương Thức Gửi Tin Nhắn:

1. **WebSocket STOMP** (Real-time) - ⭐ **Phương thức chính**
2. **REST API** (Lấy lịch sử tin nhắn)
3. **JavaScript Client** (Cho web frontend)

---

## 🔌 WebSocket API - Gửi Tin Nhắn Real-time

### 1. Kết Nối WebSocket

**URL:** `ws://localhost:8083/ws`  
**Protocol:** STOMP over WebSocket  
**Authentication:** JWT Token trong header

### 2. Gửi Tin Nhắn

**Destination:** `/app/chat.sendMessage`

**Request Payload:**
```json
{
    "receiverId": 2,
    "content": "Xin chào! Đây là tin nhắn test."
}
```

**Response:** Tin nhắn sẽ được gửi tới:
- **Người nhận:** `/user/queue/messages` 
- **Người gửi:** `/user/queue/messages` (confirmation)

**Response Format:**
```json
{
    "id": 1,
    "senderId": 1,
    "receiverId": 2,
    "content": "Xin chào! Đây là tin nhắn test.",
    "sentAt": "2025-11-04T15:30:00",
    "isRead": false,
    "senderDisplayName": "Alice Johnson"
}
```

---

## 🧪 Cách Test WebSocket với Các Tools

### 1. 🌐 Test với WebSocket Client Online

#### a) **WebSocket King** (Chrome Extension)
1. Cài đặt "WebSocket King" từ Chrome Web Store
2. URL: `ws://localhost:8083/ws/websocket`
3. Connect với headers:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### b) **Websocket.org Echo Test**
- Sử dụng: https://websocket.org/echo.html
- Nhập URL: `ws://localhost:8083/ws`

### 2. 📱 Test với Postman WebSocket (Beta)

Postman mới có tính năng WebSocket testing:

1. Tạo **New → WebSocket Request**
2. URL: `ws://localhost:8083/ws`
3. Headers: `Authorization: Bearer <token>`
4. Connect và gửi STOMP messages

### 3. 💻 Test với Command Line (wscat)

```bash
# Cài đặt wscat
npm install -g wscat

# Kết nối WebSocket
wscat -c ws://localhost:8083/ws -H "Authorization: Bearer <your-token>"

# Gửi STOMP messages
CONNECT
Authorization: Bearer <token>

SEND
destination:/app/chat.sendMessage
content-type:application/json

{"receiverId":2,"content":"Hello via wscat!"}
```

---

## 🔧 Test với JavaScript Client

### HTML Test Page
Tạo file `websocket-test.html`:

```html
<!DOCTYPE html>
<html>
<head>
    <title>WebSocket Chat Test</title>
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
</head>
<body>
    <div>
        <h2>Real Time Chat - WebSocket Test</h2>
        <div>
            <label>JWT Token:</label>
            <input type="text" id="token" placeholder="Paste JWT token here" style="width: 500px;">
        </div>
        <div>
            <button onclick="connect()">Connect</button>
            <button onclick="disconnect()">Disconnect</button>
            <span id="status">Disconnected</span>
        </div>
        <div>
            <label>Receiver ID:</label>
            <input type="number" id="receiverId" value="2">
            <label>Message:</label>
            <input type="text" id="messageContent" placeholder="Type your message...">
            <button onclick="sendMessage()">Send Message</button>
        </div>
        <div>
            <h3>Received Messages:</h3>
            <div id="messages" style="border: 1px solid #ccc; height: 300px; overflow-y: scroll; padding: 10px;"></div>
        </div>
    </div>

    <script>
        let stompClient = null;

        function connect() {
            const token = document.getElementById('token').value;
            if (!token) {
                alert('Please enter JWT token first!');
                return;
            }

            const socket = new SockJS('http://localhost:8083/ws');
            stompClient = Stomp.over(socket);
            
            const headers = {
                'Authorization': 'Bearer ' + token
            };

            stompClient.connect(headers, function(frame) {
                console.log('Connected: ' + frame);
                document.getElementById('status').textContent = 'Connected';
                
                // Subscribe to receive messages
                stompClient.subscribe('/user/queue/messages', function(message) {
                    showMessage('RECEIVED', JSON.parse(message.body));
                });
                
                // Subscribe to typing notifications
                stompClient.subscribe('/user/queue/typing', function(notification) {
                    console.log('Typing notification:', JSON.parse(notification.body));
                });
                
                // Subscribe to user status
                stompClient.subscribe('/topic/users/status', function(status) {
                    console.log('User status:', JSON.parse(status.body));
                });
                
                // Subscribe to errors
                stompClient.subscribe('/user/queue/errors', function(error) {
                    showMessage('ERROR', error.body);
                });
                
            }, function(error) {
                console.error('Connection error:', error);
                document.getElementById('status').textContent = 'Connection Failed';
                alert('Connection failed: ' + error);
            });
        }

        function disconnect() {
            if (stompClient !== null) {
                stompClient.disconnect();
                document.getElementById('status').textContent = 'Disconnected';
            }
        }

        function sendMessage() {
            if (stompClient === null) {
                alert('Please connect first!');
                return;
            }
            
            const receiverId = document.getElementById('receiverId').value;
            const content = document.getElementById('messageContent').value;
            
            if (!receiverId || !content) {
                alert('Please enter receiver ID and message content!');
                return;
            }
            
            const message = {
                receiverId: parseInt(receiverId),
                content: content
            };
            
            stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(message));
            showMessage('SENT', message);
            
            // Clear message input
            document.getElementById('messageContent').value = '';
        }

        function showMessage(type, message) {
            const messagesDiv = document.getElementById('messages');
            const messageElement = document.createElement('div');
            messageElement.style.marginBottom = '10px';
            messageElement.style.padding = '5px';
            messageElement.style.border = '1px solid #ddd';
            
            if (type === 'SENT') {
                messageElement.style.backgroundColor = '#e3f2fd';
                messageElement.innerHTML = `
                    <strong>[SENT]</strong><br>
                    To: ${message.receiverId}<br>
                    Content: ${message.content}
                `;
            } else if (type === 'RECEIVED') {
                messageElement.style.backgroundColor = '#f3e5f5';
                messageElement.innerHTML = `
                    <strong>[RECEIVED]</strong><br>
                    From: ${message.senderDisplayName} (ID: ${message.senderId})<br>
                    Content: ${message.content}<br>
                    Time: ${message.sentAt}
                `;
            } else if (type === 'ERROR') {
                messageElement.style.backgroundColor = '#ffebee';
                messageElement.innerHTML = `<strong>[ERROR]</strong><br>${message}`;
            }
            
            messagesDiv.appendChild(messageElement);
            messagesDiv.scrollTop = messagesDiv.scrollHeight;
        }

        // Enter key to send message
        document.getElementById('messageContent').addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                sendMessage();
            }
        });
    </script>
</body>
</html>
```

---

## 📋 Quy Trình Test Step-by-Step

### Bước 1: Chuẩn Bị
1. **Khởi động server**: `./mvnw spring-boot:run`
2. **Tạo users test** qua Postman:
   - Đăng ký Alice (`alice@example.com`)
   - Đăng ký Bob (`bob@example.com`)
3. **Đăng nhập Alice** và lưu JWT token

### Bước 2: Test Gửi Tin Nhắn
1. **Mở HTML test page** ở trên
2. **Paste JWT token** của Alice vào input
3. **Click Connect** → expect "Connected"
4. **Nhập Receiver ID = 2** (Bob's ID)
5. **Nhập message** và click Send
6. **Kiểm tra tin nhắn** xuất hiện trong "Received Messages"

### Bước 3: Test Nhận Tin Nhắn (2 tabs)
1. **Tab 1**: Connect với Alice token
2. **Tab 2**: Connect với Bob token (cần đăng nhập Bob trước)
3. **Gửi tin nhắn từ Alice** → Bob sẽ nhận được real-time
4. **Gửi tin nhắn từ Bob** → Alice sẽ nhận được real-time

---

## 📊 REST API - Lấy Lịch Sử Tin Nhắn

Sau khi gửi tin nhắn qua WebSocket, có thể dùng REST API để lấy lịch sử:

### 1. Lấy Lịch Sử Chat
```bash
GET /api/messages/{userId}
Authorization: Bearer <token>
```

**Example:**
```bash
GET http://localhost:8083/api/messages/2
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```json
{
    "result": "SUCCESS",
    "message": "Lấy lịch sử chat thành công",
    "data": [
        {
            "id": 1,
            "senderId": 1,
            "receiverId": 2,
            "content": "Xin chào!",
            "sentAt": "2025-11-04T15:30:00",
            "isRead": false,
            "senderDisplayName": "Alice Johnson"
        }
    ]
}
```

### 2. Lấy Tin Nhắn Chưa Đọc
```bash
GET /api/messages/unread
Authorization: Bearer <token>
```

### 3. Đánh Dấu Đã Đọc
```bash
PUT /api/messages/{messageId}/read
Authorization: Bearer <token>
```

---

## 🚨 Xử Lý Lỗi

### Lỗi Thường Gặp:

#### 1. **WebSocket Connection Failed**
```
Error: Failed to connect to WebSocket
```
**Nguyên nhân:** Server chưa chạy hoặc URL sai  
**Giải pháp:** Kiểm tra server chạy trên port 8083

#### 2. **Authentication Failed**
```
Error: HTTP 403 Forbidden
```
**Nguyên nhân:** JWT token không hợp lệ hoặc hết hạn  
**Giải pháp:** Đăng nhập lại để lấy token mới

#### 3. **Message Send Failed**
```
Error: Người nhận không tồn tại
```
**Nguyên nhân:** Receiver ID không tồn tại  
**Giải pháp:** Kiểm tra user ID có tồn tại trong database

#### 4. **Validation Error**
```
Error: Nội dung tin nhắn không được để trống
```
**Nguyên nhân:** Message content rỗng  
**Giải pháp:** Nhập nội dung tin nhắn

---

## 🔧 Debug Tips

### 1. Kiểm tra Server Logs
```bash
# Xem logs khi gửi tin nhắn
2025-11-04 15:30:00.123 INFO  --- [nio-8083-exec-1] c.l.s.controller.ChatWebSocketController : Nhận tin nhắn từ user: alice@example.com, tới user: 2
2025-11-04 15:30:00.125 INFO  --- [nio-8083-exec-1] c.l.s.controller.ChatWebSocketController : Đã gửi tin nhắn thành công: 1
```

### 2. Kiểm tra Database
```sql
-- Xem tin nhắn trong database
SELECT * FROM messages ORDER BY sent_at DESC;

-- Kiểm tra users
SELECT id, email, display_name FROM users;
```

### 3. Browser Developer Tools
```javascript
// Console commands để debug
console.log('WebSocket state:', stompClient.ws.readyState);
// 0: CONNECTING, 1: OPEN, 2: CLOSING, 3: CLOSED
```

---

## 🎯 Test Scenarios

### Scenario 1: Basic Message Flow
```
1. Alice connects ✅
2. Alice sends message to Bob ✅
3. Bob receives message ✅  
4. Check message in database ✅
```

### Scenario 2: Bidirectional Chat
```
1. Alice và Bob both connect ✅
2. Alice sends "Hello Bob" ✅
3. Bob receives và replies "Hi Alice" ✅
4. Alice receives Bob's reply ✅
```

### Scenario 3: Multiple Users
```
1. Alice, Bob, Charlie all connect ✅
2. Alice sends to Bob ✅
3. Alice sends to Charlie ✅
4. Bob sends to Charlie ✅
5. All messages delivered correctly ✅
```

### Scenario 4: Error Handling
```
1. Send message without authentication → 403 ❌
2. Send to non-existent user → error message ❌
3. Send empty message → validation error ❌
4. Network disconnect → reconnection ❌
```

---

## 📱 Mobile Testing

### React Native / Flutter
```javascript
// React Native với react-native-stomp-websocket
import Stomp from 'react-native-stomp-websocket';

const client = Stomp.over(() => new WebSocket('ws://localhost:8083/ws'));
client.connect({
    'Authorization': 'Bearer ' + token
}, (frame) => {
    client.subscribe('/user/queue/messages', (message) => {
        console.log('Received:', JSON.parse(message.body));
    });
    
    client.send('/app/chat.sendMessage', {}, JSON.stringify({
        receiverId: 2,
        content: 'Hello from mobile!'
    }));
});
```

---

## ✅ Checklist Test Hoàn Tất

### WebSocket Testing
- [ ] Kết nối WebSocket thành công
- [ ] Gửi tin nhắn qua `/app/chat.sendMessage`
- [ ] Nhận tin nhắn qua `/user/queue/messages`
- [ ] Test với nhiều users cùng lúc
- [ ] Test error handling

### REST API Testing  
- [ ] Lấy lịch sử chat sau khi gửi tin nhắn
- [ ] Kiểm tra tin nhắn unread
- [ ] Đánh dấu tin nhắn đã đọc
- [ ] Verify data trong database

### Integration Testing
- [ ] WebSocket + REST API hoạt động cùng nhau
- [ ] Real-time notifications
- [ ] User status updates
- [ ] Typing indicators

---

**🎉 Chúc mừng! Bạn đã nắm vững cách test API gửi tin nhắn real-time!**

*Lưu ý: WebSocket testing phức tạp hơn REST API, hãy kiên nhẫn và test từng bước một.*
