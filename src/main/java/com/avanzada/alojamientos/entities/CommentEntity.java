package com.avanzada.alojamientos.entities;

import com.avanzada.alojamientos.DTO.model.HostReply;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Integer AUTO_INCREMENT
    private Long id;

    @Column(nullable = false)
    private Integer rating; // la validación de rango se puede manejar en lógica de negocio

    @Column(nullable = false, columnDefinition = "TEXT", length = 500, name="comment_text")
    private String text;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Boolean isModerated = false;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private ReservationEntity reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id", nullable = false)
    private AccommodationEntity accommodation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Embedded
    private HostReply hostReply;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

