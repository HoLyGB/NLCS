package com.chonhanh.repository;

import com.chonhanh.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
//    // Lấy tất cả các hộp thoại chat của một User (Xếp theo thời gian mới nhất lên đầu)
//    @Query("SELECT c FROM Conversation c WHERE c.buyer.id = :userId OR c.seller.id = :userId ORDER BY c.createdAt DESC")
//    List<Conversation> findAllByUserId(@Param("userId") Long userId);
	// Trong ConversationRepository.java
	@Query("SELECT c FROM Conversation c LEFT JOIN Message m ON m.conversation = c WHERE c.buyer.id = :userId OR c.seller.id = :userId GROUP BY c.id ORDER BY MAX(m.createdAt) DESC")
    List<Conversation> findAllByUserId(@Param("userId") Long userId);
    // Kiểm tra xem giữa 2 người này đã từng có cuộc trò chuyện nào về sản phẩm này chưa
    Optional<Conversation> findByPostIdAndBuyerIdAndSellerId(Long postId, Long buyerId, Long sellerId);
    @Modifying
    @Query("DELETE FROM Conversation c WHERE c.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}