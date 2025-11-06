# Hướng Dẫn Xóa Tin Nhắn (Message Deletion)

## Tổng Quan
Chức năng xóa tin nhắn cho phép người dùng xóa tin nhắn đã gửi hoặc nhận. Khi xóa, **cả người gửi và người nhận đều không thấy tin nhắn** đó nữa (soft delete).

## Cách Hoạt Động

### 1. Backend Changes

#### Database Schema
```sql
ALTER TABLE messages 
ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
```

#### Entity (Message.java)
- Thêm field `isDeleted` (Boolean)
- Default value: `false`

#### Repository (MessageRepository.java)
- Tất cả query đã được cập nhật để lọc bỏ tin nhắn đã xóa: `AND m.isDeleted = false`
- Queries affected:
  - `findChatHistory()` - Lịch sử chat
  - `findUnreadMessages()` - Tin nhắn chưa đọc
  - `countUnreadMessages()` - Đếm tin nhắn chưa đọc
  - `findLatestMessages()` - Tin nhắn mới nhất

#### Service (MessageService.java)
```java
void deleteMessage(Long messageId, Long currentUserId);
```
- **Soft delete**: Đánh dấu `isDeleted = true` thay vì xóa khỏi database
- **Authorization**: Chỉ người gửi hoặc người nhận mới có thể xóa
- Throws `IllegalArgumentException` nếu không có quyền

#### REST API
```http
DELETE /api/messages/{messageId}
Authorization: Bearer <JWT_TOKEN>

Response:
{
  "result": "SUCCESS",
  "message": "Xóa tin nhắn thành công",
  "data": null
}
```

#### WebSocket
```
Destination: /app/chat.deleteMessage
Payload: {
  "messageId": 123
}

Broadcast: /topic/messages/delete
Response: {
  "messageId": 123,
  "deletedBy": 1,
  "action": "DELETE"
}
```

### 2. Frontend Changes

#### WebSocket Service (websocket.service.ts)
```typescript
// Subscribe to delete notifications
messageDeletes$: Observable<{messageId: number, deletedBy: number}>

// Send delete request
deleteMessage(messageId: number): void
```

#### Chat Window Component
```typescript
// Subscribe to real-time deletes
subscribeToMessageDeletes(): void

// Delete message with confirmation
deleteMessage(messageId: number): void
```

#### UI/UX
- **Nút xóa**: Hiển thị khi hover vào message bubble
- **Position**: 
  - Tin nhắn nhận (trái): Actions ở bên trái bubble
  - Tin nhắn gửi (phải): Actions ở bên phải bubble
- **Confirm dialog**: "Bạn có chắc chắn muốn xóa tin nhắn này? Cả bạn và người nhận đều sẽ không thấy tin nhắn này nữa."
- **Real-time update**: Tin nhắn biến mất ngay lập tức cho cả 2 người dùng

## Cách Sử Dụng

### 1. Chạy Migration SQL
```bash
# PostgreSQL
psql -U postgres -d chatapp -f server/add_is_deleted_column.sql

# MySQL
mysql -u root -p chatapp < server/add_is_deleted_column.sql
```

### 2. Restart Backend
```bash
cd server
mvn spring-boot:run
```

### 3. Test Chức Năng

#### Via UI:
1. Login vào 2 tài khoản (2 browser/tabs khác nhau)
2. Gửi tin nhắn giữa 2 users
3. Hover vào tin nhắn → click icon thùng rác (🗑️)
4. Confirm xóa
5. Verify: Tin nhắn biến mất ở **cả 2 phía**

#### Via REST API:
```bash
# Get JWT token first
TOKEN="your_jwt_token_here"

# Delete message
curl -X DELETE http://localhost:8083/api/messages/123 \
  -H "Authorization: Bearer $TOKEN"
```

#### Via WebSocket:
```javascript
stompClient.send("/app/chat.deleteMessage", {}, JSON.stringify({
  messageId: 123
}));
```

## Security

### Authorization Rules
- ✅ Người gửi có thể xóa
- ✅ Người nhận có thể xóa
- ❌ User khác không thể xóa

### Data Privacy
- **Soft delete**: Tin nhắn vẫn tồn tại trong database (có thể khôi phục nếu cần)
- **Query filtering**: Tất cả queries tự động lọc bỏ tin nhắn đã xóa
- **Reactions**: Reactions của tin nhắn đã xóa vẫn tồn tại (nhưng không hiển thị)

## Troubleshooting

### Tin nhắn không biến mất
1. Check WebSocket connection: `isConnected$ | async`
2. Check console logs: "Received message delete notification"
3. Verify JWT token còn valid
4. Check browser console for errors

### Lỗi 403 Forbidden
- User không có quyền xóa tin nhắn đó
- Verify: User phải là sender hoặc receiver

### Tin nhắn vẫn hiển thị sau khi xóa
- Clear browser cache
- Reload lại chat history
- Check database: `SELECT * FROM messages WHERE id = X;` → `is_deleted` phải là `true`

## Future Enhancements (Optional)

1. **Undo Delete**: Cho phép khôi phục tin nhắn trong 30 giây
2. **Delete for Me Only**: Chỉ xóa ở phía người xóa, người kia vẫn thấy
3. **Batch Delete**: Xóa nhiều tin nhắn cùng lúc
4. **Auto-delete**: Tự động xóa tin nhắn sau X ngày
5. **Admin Delete**: Admin có thể xóa bất kỳ tin nhắn nào
6. **Hard Delete**: Xóa vĩnh viễn khỏi database (GDPR compliance)

## Testing Checklist

- [ ] Migration SQL chạy thành công
- [ ] Backend compile không lỗi
- [ ] Frontend compile không lỗi
- [ ] REST API DELETE /api/messages/{id} hoạt động
- [ ] WebSocket /app/chat.deleteMessage hoạt động
- [ ] Tin nhắn biến mất real-time cho cả 2 users
- [ ] Authorization: Chỉ sender/receiver mới xóa được
- [ ] Confirm dialog hiển thị
- [ ] UI: Nút delete hiện khi hover
- [ ] UI: Position đúng cho sent/received messages
- [ ] Query lọc bỏ tin nhắn đã xóa
- [ ] Không crash khi xóa tin nhắn không tồn tại
