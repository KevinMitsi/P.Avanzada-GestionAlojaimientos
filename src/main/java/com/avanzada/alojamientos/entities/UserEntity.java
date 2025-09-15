package com.avanzada.alojamientos.entities;

import com.avanzada.alojamientos.DTO.model.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Validación trasladada al schema
    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    // Contraseña con mínimo 8 caracteres
    @Column(nullable = false, length = 255)
    private String password;

    // Teléfono con regex SQL
    @Column(name = "phone", columnDefinition = "VARCHAR(15) CHECK (phone ~ '^\\+?[0-9]{8,15}$')")
    private String phone;

    // Fecha de nacimiento debe ser pasada → CHECK en DB
    @Column(name = "date_of_birth", columnDefinition = "DATE CHECK (date_of_birth < CURRENT_DATE)")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserRole role;

    @Column(length = 500)
    private String avatarUrl;

    // Máximo 1000 caracteres
    @Column(columnDefinition = "TEXT CHECK (char_length(description) <= 1000)")
    private String description;

    @ElementCollection
    @CollectionTable(name = "user_documents", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "document_url")
    private List<String> documentsUrl = new ArrayList<>();

    @Column(nullable = false)
    private Boolean verified = false;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean deleted = false;

    // Relaciones
    @OneToMany(mappedBy = "host", cascade = CascadeType.ALL)
    private List<AccommodationEntity> accommodations = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ReservationEntity> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<CommentEntity> comments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<FavoriteEntity> favorites = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<NotificationEntity> notifications = new ArrayList<>();

    @OneToOne(mappedBy = "host", cascade = CascadeType.ALL)
    private HostProfileEntity hostProfile;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
