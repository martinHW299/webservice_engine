package com.tigo.api.ws_engine.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tst_wse_ws")
public class Webservice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ws_id", updatable = false, nullable = false, length = 64, unique = true)
    private UUID id;

    private String md5;

    private String tag;

    private String name;

    private String description;

    private String uid;

    @Lob
    private String content;

    private String parameters;

    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private String changedBy;

    @OneToMany(mappedBy = "webservice", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Request> requests;
    
}


