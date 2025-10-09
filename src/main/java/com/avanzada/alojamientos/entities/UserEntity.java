package com.avanzada.alojamientos.entities;

import com.avanzada.alojamientos.DTO.model.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Validación trasladada al schema
    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    // Contraseña con mínimo 8 caracteres
    @Column(nullable = false)
    private String password;

    // Teléfono con regex SQL
    @Column(name = "phone", nullable = false)
    private String phone;

    // Fecha de nacimiento debe ser pasada → CHECK en DB
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    // Cambio: roles múltiples en lugar de un solo rol
    @ElementCollection(targetClass = UserRole.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<UserRole> roles = new HashSet<>();

    // Máximo 1000 caracteres
    @Column
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
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

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "profile_image_id")
    private ImageEntity profileImage;

    // Métodos de conveniencia para manejar roles
    public void addRole(UserRole role) {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(role);
    }


    public boolean hasRole(UserRole role) {
        return this.roles != null && this.roles.contains(role);
    }

    public boolean isHost() {
        return hasRole(UserRole.HOST);
    }

    public boolean isUser() {
        return hasRole(UserRole.USER);
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (roles == null || roles.isEmpty()) {
            roles = new HashSet<>();
            roles.add(UserRole.USER);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
