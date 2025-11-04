# 🚀 Hướng Dẫn Sử Dụng Postman Để Test API

## 📦 Cài Đặt & Import Collection

### 1. Cài đặt Postman
- Tải và cài đặt Postman từ: https://www.postman.com/downloads/
- Hoặc sử dụng Postman Web tại: https://web.postman.co/

### 2. Import Collection
1. Mở Postman
2. Click **Import** ở góc trên bên trái
3. Chọn file `Real_Time_Chat_API.postman_collection.json`
4. Click **Import**

### 3. Thiết lập Environment
1. Click biểu tượng **Environment** (👁️) ở góc trên bên phải
2. Click **Add** để tạo environment mới
3. Đặt tên: `Real Time Chat - Local`
4. Thêm các variables:
   - `baseUrl`: `http://localhost:8083`
   - `authToken`: (để trống, sẽ tự động set khi đăng nhập)
   - `currentUserId`: (để trống)
   - `currentUserEmail`: (để trống)
5. Click **Save**
6. Chọn environment vừa tạo từ dropdown

---

## 🔧 Khởi Động Server

### 1. Khởi động PostgreSQL Database
```bash
# Đảm bảo PostgreSQL đang chạy trên port 5432
# Database name: chatdb
# Username: postgres  
# Password: Minh2263@
```

### 2. Chạy Spring Boot Application
```bash
cd D:\Code\Projects\RealTimeChat\server
./mvnw spring-boot:run
```

**Hoặc từ IDE:**
- Mở project trong IntelliJ IDEA / Eclipse
- Chạy class `ServerApplication.java`

Server sẽ chạy trên: http://localhost:8083

---

## 📋 Quy Trình Test API Theo Thứ Tự

### Bước 1: Test Kết Nối Server
1. Mở folder **🧪 Test Endpoints**
2. Chạy **Test Public Endpoint**
3. Expect: Status 200, message "Đây là endpoint public..."

### Bước 2: Đăng Ký Users
1. Mở folder **🔑 Authentication**
2. Chạy **Đăng Ký User Mới** (Alice)
3. Mở folder **📝 Sample Requests**
4. Chạy **Đăng Ký User Thứ 2** (Bob)
5. Chạy **Đăng Ký User Thứ 3** (Charlie)

**Expected:** Tất cả trả về status 201 với thông tin user

### Bước 3: Đăng Nhập & Lấy Token
1. Quay lại folder **🔑 Authentication**
2. Chạy **Đăng Nhập** với Alice
3. **Token sẽ tự động được lưu vào environment variable `authToken`**
4. Kiểm tra: Click vào Environment variable, thấy `authToken` đã có giá trị

### Bước 4: Test Protected Endpoints
1. Mở folder **🧪 Test Endpoints**
2. Chạy **Test Protected Endpoint**
3. Expect: Status 200, message "Đây là endpoint protected..."

### Bước 5: Test User Management
1. Mở folder **👥 User Management**
2. Chạy **Lấy Thông Tin User Hiện Tại**
3. Chạy **Lấy Danh Sách Tất Cả Users** → expect có 3 users
4. Chạy **Lấy Thông Tin User Theo ID** → thay ID = 2 (Bob)
5. Chạy **Tìm Kiếm Users Theo Tên** → thay query = "bob"

### Bước 6: Test Messages (Chưa có data)
1. Mở folder **💬 Messages** 
2. Chạy **Lấy Lịch Sử Chat Với User** → expect array rỗng []
3. Chạy **Lấy Tin Nhắn Chưa Đọc** → expect array rỗng []

---

## 🔄 Test Scenarios Chi Tiết

### Scenario 1: Authentication Flow
```
1. Đăng ký user mới ✅
2. Đăng nhập với user vừa tạo ✅
3. Lấy thông tin user hiện tại ✅
4. Đăng xuất ✅
```

### Scenario 2: User Management
```
1. Tạo 3 users: Alice, Bob, Charlie ✅
2. Đăng nhập với Alice ✅
3. Lấy danh sách tất cả users → expect thấy Bob, Charlie ✅
4. Lấy thông tin Bob theo ID ✅
5. Tìm kiếm "bob" → expect thấy Bob ✅
```

### Scenario 3: Error Handling
```
1. Đăng ký với email đã tồn tại → expect 400 Bad Request ❌
2. Đăng nhập với password sai → expect 401 Unauthorized ❌
3. Gọi protected endpoint không có token → expect 401 ❌
4. Gọi API với token invalid → expect 401 ❌
```

---

## 🐛 Troubleshooting

### ❌ Lỗi Connection Refused
**Nguyên nhân:** Server chưa chạy
**Giải pháp:**
```bash
cd D:\Code\Projects\RealTimeChat\server
./mvnw spring-boot:run
```

### ❌ Lỗi 401 Unauthorized  
**Nguyên nhân:** Token hết hạn hoặc không có token
**Giải pháp:**
1. Đăng nhập lại để lấy token mới
2. Kiểm tra Environment variable `authToken` có giá trị
3. Kiểm tra header `Authorization: Bearer {{authToken}}`

### ❌ Lỗi Database Connection
**Nguyên nhân:** PostgreSQL không chạy hoặc config sai
**Giải pháp:**
1. Khởi động PostgreSQL service
2. Tạo database `chatdb`
3. Kiểm tra config trong `application.properties`

### ❌ Lỗi 400 Bad Request
**Nguyên nhân:** Dữ liệu request không hợp lệ
**Giải pháp:**
1. Kiểm tra JSON format
2. Kiểm tra required fields
3. Kiểm tra validation rules (email format, password length, etc.)

---

## 📊 Automated Testing Scripts

Collection đã có sẵn test scripts tự động:

### Test Scripts trong "Đăng Nhập"
```javascript
// Tự động lưu token vào environment
pm.test("Save auth token", function () {
    var jsonData = pm.response.json();
    pm.environment.set('authToken', jsonData.data.token);
    pm.environment.set('currentUserId', jsonData.data.user.id);
});
```

### Chạy tất cả tests
1. Click **Run** trên collection
2. Chọn tất cả requests
3. Click **Run Real Time Chat API**
4. Xem kết quả tests tự động

---

## 🌟 Advanced Usage

### 1. Pre-request Scripts
Thêm script chạy trước mỗi request:
```javascript
// Kiểm tra server có online không
pm.sendRequest({
    url: pm.environment.get('baseUrl') + '/api/test/public',
    method: 'GET'
}, function (err, res) {
    if (err) {
        throw new Error('Server is not running!');
    }
});
```

### 2. Dynamic Variables
Sử dụng variables động:
```javascript
// Tạo email ngẫu nhiên
pm.environment.set('randomEmail', `user${Math.floor(Math.random() * 1000)}@example.com`);
```

### 3. Chain Requests
Gọi request khác từ test script:
```javascript
// Tự động đăng nhập sau khi đăng ký
pm.sendRequest({
    url: pm.environment.get('baseUrl') + '/api/auth/login',
    method: 'POST',
    header: {'Content-Type': 'application/json'},
    body: {
        mode: 'raw',
        raw: JSON.stringify({
            email: 'alice@example.com',
            password: 'password123'
        })
    }
}, function (err, res) {
    var loginData = res.json();
    pm.environment.set('authToken', loginData.data.token);
});
```

---

## 📈 Performance Testing

### 1. Load Testing
- Sử dụng Newman (CLI version của Postman)
- Chạy collection với nhiều iterations

```bash
npm install -g newman
newman run Real_Time_Chat_API.postman_collection.json -e environment.json -n 100
```

### 2. Monitor Response Times
Thêm test để check performance:
```javascript
pm.test("Response time is less than 500ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(500);
});
```

---

## 🔐 WebSocket Testing

**Lưu ý:** Postman không hỗ trợ WebSocket testing tốt. Để test WebSocket:

### 1. Sử dụng WebSocket Client Tools:
- **Postman WebSocket (Beta)**: Tính năng mới của Postman
- **WebSocket King**: Chrome extension
- **wscat**: Command line tool
- **Custom HTML page**: Tạo simple client

### 2. WebSocket Connection Example:
```javascript
// URL: ws://localhost:8083/ws
// Protocol: STOMP
// Auth: JWT token trong header

// Connect:
const socket = new SockJS('http://localhost:8083/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({'Authorization': 'Bearer ' + token}, function(frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe to messages
    stompClient.subscribe('/user/queue/messages', function(message) {
        console.log('Received: ' + message.body);
    });
    
    // Send message
    stompClient.send('/app/chat.sendMessage', {}, JSON.stringify({
        'receiverId': 2,
        'content': 'Hello Bob!'
    }));
});
```

---

## ✅ Checklist Hoàn Thành

### Cơ Bản
- [ ] Import collection thành công
- [ ] Thiết lập environment với baseUrl
- [ ] Server chạy thành công trên port 8083
- [ ] Test public endpoint thành công
- [ ] Đăng ký user thành công
- [ ] Đăng nhập và nhận token thành công
- [ ] Test protected endpoint với token thành công

### Nâng Cao  
- [ ] Tạo được nhiều users
- [ ] Test tất cả user management APIs
- [ ] Test error cases (wrong password, duplicate email, etc.)
- [ ] Test message APIs (dù chưa có data)
- [ ] Hiểu được cấu trúc response ApiResponse<T>
- [ ] Biết cách debug khi có lỗi

### Chuyên Sâu
- [ ] Chạy automated test scripts
- [ ] Tùy chỉnh environment variables
- [ ] Tạo test scenarios phức tạp
- [ ] Test performance và response time
- [ ] Hiểu được JWT token flow
- [ ] Sẵn sàng test WebSocket với tools khác

---

**🎉 Chúc mừng! Bạn đã nắm vững cách test toàn bộ API của Real Time Chat application!**
