package com.pms.propertymanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_consersations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatConversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "tenant_id")
    private User tenant;

    @ManyToOne
    @JoinColumn(name = "host_id")
    private User host;

    @Column(name = "last_message")
    private String lastMessage; // Tin nhắn cuối

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt; // Thời gian tin cuối

    @Column(name = "unread_count_tenant")
    private Integer unreadCountTenant = 0; // Số tin chưa đọc của tenant

    @Column(name = "unread_count_host")
    private Integer unreadCountHost = 0; // Số tin chưa đọc của host

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Danh sách tin nhắn
    @OneToMany(mappedBy = "conversation")
    @OrderBy("createdAt ASC") // Sắp xếp theo thời gian tăng dần
    private List<ChatMessage> messages = new ArrayList<>();

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
