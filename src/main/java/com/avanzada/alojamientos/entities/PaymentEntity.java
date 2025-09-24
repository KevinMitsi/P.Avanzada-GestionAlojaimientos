package com.avanzada.alojamientos.entities;

import com.avanzada.alojamientos.DTO.model.PaymentMethod;
import com.avanzada.alojamientos.DTO.model.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT con Integer
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2) // Monto obligatorio
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private LocalDateTime paidAt;

    @OneToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private ReservationEntity reservation;
}

