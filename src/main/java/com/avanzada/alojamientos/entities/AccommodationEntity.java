package com.avanzada.alojamientos.entities;

import com.avanzada.alojamientos.DTO.model.Coordinates;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "accommodations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 200,
            columnDefinition = "VARCHAR(200) CHECK (char_length(title) >= 5)")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 500)
    private String address;

    @Embedded
    private Coordinates coordinates;

    @Column(nullable = false, precision = 10, scale = 2,
            columnDefinition = "DECIMAL(10,2) CHECK (price_per_night > 0)")
    private Double pricePerNight;

    @ElementCollection
    @CollectionTable(name = "accommodation_services", joinColumns = @JoinColumn(name = "accommodation_id"))
    @Column(name = "service")
    private Set<String> services = new HashSet<>();

    // Número de huéspedes mínimo = 1
    @Column(nullable = false,
            columnDefinition = "INT CHECK (max_guests >= 1)")
    private Integer maxGuests;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Boolean softDeleted = false;

    private LocalDateTime deletedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private UserEntity host;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "city_id", nullable = false)
    private CityEntity city;

    @OneToMany(mappedBy = "accommodation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImageEntity> images = new ArrayList<>();

//    @OneToMany(mappedBy = "accommodation", cascade = CascadeType.ALL)
//    private List<Reservation> reservations = new ArrayList<>();
//
//    @OneToMany(mappedBy = "accommodation", cascade = CascadeType.ALL)
//    private List<Comment> comments = new ArrayList<>();
//
//    @OneToMany(mappedBy = "accommodation", cascade = CascadeType.ALL)
//    private List<Favorite> favorites = new ArrayList<>();

    @Transient
    private Integer countReservations;

    @Transient
    private Double avgRating;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

//    public Integer getCountReservations() {
//        return reservations != null ? reservations.size() : 0;
//    }
//
//    public Double getAvgRating() {
//        if (comments == null || comments.isEmpty()) {
//            return 0.0;
//        }
//        return comments.stream()
//                .mapToInt(Comment::getRating)
//                .average()
//                .orElse(0.0);
//    }
}


