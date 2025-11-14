package com.avanzada.alojamientos.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    private Long id;

    @Column(nullable = false, length = 500) // URL obligatoria
    private String url;

    @Column(length = 500)
    private String thumbnailUrl;

    // Campos específicos para Cloudinary
    @Column(length = 200)
    private String cloudinaryPublicId;

    @Column(length = 500)
    private String cloudinaryThumbnailUrl;

    @Column(nullable = false)
    private Boolean isPrimary = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "accommodation_id")
    private AccommodationEntity accommodation;

    @OneToOne(mappedBy = "profileImage")
    private UserEntity user;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
