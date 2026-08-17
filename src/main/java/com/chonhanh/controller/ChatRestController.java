package com.chonhanh.controller;

import com.chonhanh.entity.Conversation;
import com.chonhanh.entity.Message;
import com.chonhanh.entity.User;
import com.chonhanh.repository.MessageRepository;
import com.chonhanh.service.ChatService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;
    private final MessageRepository messageRepository;

    // 1. Lấy hoặc tạo hội thoại khi bấm "Liên hệ" từ trang chi tiết sản phẩm
    @GetMapping("/start/{postId}")
    public ResponseEntity<Long> startChat(@PathVariable Long postId, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return ResponseEntity.status(401).build(); // Chưa đăng nhập
        }

        Conversation conv = chatService.getOrCreateConversation(postId, user.getId());
        return ResponseEntity.ok(conv.getId()); // Trả về ID của phòng chat
    }

    // 2. Lấy danh sách tin nhắn của phòng chat
    @GetMapping("/messages/{conversationId}")
    public ResponseEntity<List<Message>> getMessages(@PathVariable Long conversationId) {
        // Bạn cần đảm bảo MessageRepository có hàm findByConversationIdOrderByCreatedAtAsc
        List<Message> messages = messageRepository.findByConversationIdAndIsReadFalse(conversationId);
        return ResponseEntity.ok(messages);
    }

    // 3. Gửi tin nhắn nhanh qua AJAX
    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@RequestParam Long conversationId, 
                                               @RequestParam String content, 
                                               HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        Message savedMsg = chatService.sendMessage(conversationId, user.getId(), content);
        return ResponseEntity.ok(savedMsg);
    }
}