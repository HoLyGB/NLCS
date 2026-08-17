package com.chonhanh.controller;

import com.chonhanh.entity.Conversation;
import com.chonhanh.entity.Message;
import com.chonhanh.entity.User;
import com.chonhanh.repository.ConversationRepository;
import com.chonhanh.repository.MessageRepository;
import com.chonhanh.service.ChatService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    // 1. Bấm nút "Liên hệ" từ trang chi tiết sản phẩm
    @GetMapping("/start/{postId}")
    public String startChat(@PathVariable Long postId, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        Conversation conversation = chatService.getOrCreateConversation(postId, user.getId());
        return "redirect:/chat/room/" + conversation.getId();
    }

    // 2. Vào phòng chat
    @GetMapping("/room/{conversationId}")
    public String chatRoom(@PathVariable Long conversationId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Hội thoại không tồn tại"));

        // Lấy danh sách tin nhắn (mới nhất hiển thị ở dưới)
        Pageable pageable = PageRequest.of(0, 50, Sort.by("createdAt").ascending());
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable).getContent();

        model.addAttribute("currentUserId", user.getId());
        model.addAttribute("conversation", conversation);
        model.addAttribute("messages", messages);
        chatService.markMessagesAsRead(conversationId, user.getId()); // Thêm dòng này
        return "chat-room"; // Tên file giao diện chat-room.html
    }

    // 3. Xử lý gửi tin nhắn
    @PostMapping("/send")
    public String sendMessage(@RequestParam Long conversationId, 
                              @RequestParam String content, 
                              HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        if (content != null && !content.trim().isEmpty()) {
            chatService.sendMessage(conversationId, user.getId(), content.trim());
        }

        return "redirect:/chat/room/" + conversationId;
    }
 // 1. Hiển thị trang danh sách hộp thoại tin nhắn của tôi
    @GetMapping("/inbox")
    public String inbox(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        List<Conversation> conversations = chatService.getUserConversations(user.getId());
        
        model.addAttribute("conversations", conversations);
        model.addAttribute("currentUserId", user.getId());
        
        return "chat-inbox"; // Tên file giao diện danh sách chat
    }
}