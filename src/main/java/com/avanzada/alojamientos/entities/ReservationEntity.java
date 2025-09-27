package com.avanzada.alojamientos.entities;

import com.avanzada.alojamientos.DTO.model.CancelledBy;
import com.avanzada.alojamientos.DTO.model.ReservationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    private Long id;

    // Fecha de inicio obligatoria
    @Column(nullable = false)
    private LocalDate startDate;

    // Fecha de fin obligatoria
    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Integer nights;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime cancelledAt;

    @Column(length = 500)
    private String motivoCancelacion;

    @Enumerated(EnumType.STRING)
    private CancelledBy cancelledBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id", nullable = false, columnDefinition = "BIGINT UNSIGNED")
    private AccommodationEntity accommodation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL)
    private PaymentEntity payment;

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL)
    private CommentEntity comment;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (startDate != null && endDate != null) {
            nights = (int) ChronoUnit.DAYS.between(startDate, endDate);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
