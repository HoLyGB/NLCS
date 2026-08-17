package com.chonhanh.repository;

import com.chonhanh.entity.Conversation;
import com.chonhanh.entity.Message;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
	// Thêm vào MessageRepository.java
	Optional<Message> findTopByConversationIdOrderByCreatedAtDesc(Long conversationId);
	
	List<Message> findByConversationIdAndIsReadFalse(Long conversationId);
    // Lấy lịch sử tin nhắn của một cuộc hội thoại cụ thể (Phân trang để tin nhắn tải dần lên khi kéo)
    Page<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);
 // Trong MessageRepository.java
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation IN :conversations AND m.isRead = false AND m.sender.id != :userId")
    long countUnreadMessages(@Param("conversations") List<Conversation> conversations, @Param("userId") Long userId);
 // Thêm vào MessageRepository.java
    boolean existsByConversationIdAndIsReadFalseAndSenderIdNot(Long conversationId, Long userId);
 // Thêm vào MessageRepository.java
    @Query("SELECT COUNT(m) > 0 FROM Message m WHERE m.conversation.id = :convId AND m.isRead = false AND m.sender.id != :userId")
    boolean hasUnreadMessage(@Param("convId") Long conversationId, @Param("userId") Long userId);
    @Modifying
    @Query("DELETE FROM Message m WHERE m.conversation.post.id = :postId")
    void deleteMessagesByPostId(@Param("postId") Long postId);
}