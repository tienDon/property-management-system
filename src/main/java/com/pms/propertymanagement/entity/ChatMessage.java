package com.pms.propertymanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conversation_id")
    private ChatConversation conversation;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender; // Người gửi

    @Column(name = "message_content")
    @NotBlank(message = "Message cannot be empty")
    @Size(min = 1, max = 2000, message = "Message must be 1-2000 characters")
    private String messageContent; // Nội dung tin nhắn

    @Column(name = "message_type")
    private String messageType = "TEXT"; // TEXT, IMAGE, FILE

    @Column(name = "is_read")
    private Boolean isRead = false; // Đã đọc chưa

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
