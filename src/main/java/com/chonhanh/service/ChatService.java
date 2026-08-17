package com.chonhanh.service;

import com.chonhanh.entity.Conversation;
import com.chonhanh.entity.Message;
import com.chonhanh.entity.Post;
import com.chonhanh.entity.User;
import com.chonhanh.repository.ConversationRepository;
import com.chonhanh.repository.MessageRepository;
import com.chonhanh.repository.PostRepository;
import com.chonhanh.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * Lấy cuộc hội thoại đã có hoặc tạo mới nếu chưa tồn tại
     */
    @Transactional
    public Conversation getOrCreateConversation(Long postId, Long buyerId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài đăng"));

        Long sellerId = post.getUser().getId();

        // Chặn người mua nhắn tin cho chính mình
        if (buyerId.equals(sellerId)) {
            throw new RuntimeException("Bạn không thể tự nhắn tin cho chính mình!");
        }

        // Tìm hội thoại đã tồn tại giữa 2 người về bài đăng này
        return conversationRepository.findByPostIdAndBuyerIdAndSellerId(postId, buyerId, sellerId)
                .orElseGet(() -> {
                    // Nếu chưa có, tạo mới
                    Conversation newConv = new Conversation();
                    newConv.setPost(post);
                    newConv.setBuyer(userRepository.findById(buyerId)
                            .orElseThrow(() -> new RuntimeException("Người mua không tồn tại")));
                    newConv.setSeller(userRepository.findById(sellerId)
                            .orElseThrow(() -> new RuntimeException("Người bán không tồn tại")));
                    return conversationRepository.save(newConv);
                });
    }

    /**
     * Lưu tin nhắn vào Database
     */
    @Transactional
    public Message sendMessage(Long conversationId, Long senderId, String content) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Hội thoại không tồn tại"));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Người gửi không tồn tại"));

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(content);

        return messageRepository.save(message);
    }
    
    /**
     * Lấy danh sách hội thoại của người dùng
     */
    public List<Conversation> getUserConversations(Long userId) {
        return conversationRepository.findAllByUserId(userId);
    }
 // Trong ChatService.java
    @Transactional
    public void markMessagesAsRead(Long conversationId, Long userId) {
        // Tìm tất cả tin nhắn chưa đọc trong hội thoại này mà mình là người nhận
        List<Message> unreadMessages = messageRepository.findByConversationIdAndIsReadFalse(conversationId);
        
        for (Message msg : unreadMessages) {
            if (!msg.getSender().getId().equals(userId)) {
                msg.setRead(true);
            }
        }
        messageRepository.saveAll(unreadMessages);
    }


    // Hàm phụ trợ lấy thời gian tin nhắn cuối cùng của phòng chat
    private LocalDateTime getLatestMessageTime(Long conversationId) {
        Optional<Message> latestMsg = messageRepository.findTopByConversationIdOrderByCreatedAtDesc(conversationId);
        if (latestMsg.isPresent() && latestMsg.get().getCreatedAt() != null) {
            return latestMsg.get().getCreatedAt();
        }
        // Nếu phòng chat mới tạo chưa có tin nhắn nào, trả về thời điểm tối thiểu để đẩy xuống dưới
        return LocalDateTime.MIN; 
    }
 // Thêm vào ChatService.java
    public boolean hasUnread(Long conversationId, Long userId) {
        return messageRepository.hasUnreadMessage(conversationId, userId);
    }
}