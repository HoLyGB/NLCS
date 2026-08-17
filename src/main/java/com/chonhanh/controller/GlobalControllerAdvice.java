package com.chonhanh.controller;

import com.chonhanh.entity.Conversation;
import com.chonhanh.entity.User;
import com.chonhanh.repository.ConversationRepository;
import com.chonhanh.repository.MessageRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @ModelAttribute
    public void addGlobalAttributes(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user != null) {
            List<Conversation> myConvs = conversationRepository.findAllByUserId(user.getId());
            long unreadCount = messageRepository.countUnreadMessages(myConvs, user.getId());
            model.addAttribute("unreadCount", unreadCount);
        }
    }
}