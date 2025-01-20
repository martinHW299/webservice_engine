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
@Table(name = "tst_wse_src")
public class Source {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "source_id", updatable = false, nullable = false, length = 64, unique = true)
    private UUID id;

    private String tag;

    private String url;

    private String usr;

    private String pwd;

    @Column(name = "pool_size")
    private Integer poolSize;

    private Integer timeout;

    @Column(name = "idle_timeout")
    private Integer idleTimeout;

    @Column(name = "max_lifetime")
    private Integer maxLifetime;

    @Column(name = "min_idle")
    private Integer minIdle;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private String status;

    @OneToMany(mappedBy = "source", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Request> requests;
}
