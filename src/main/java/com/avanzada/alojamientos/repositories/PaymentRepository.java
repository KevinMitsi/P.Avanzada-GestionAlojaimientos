package com.avanzada.alojamientos.repositories;

import com.avanzada.alojamientos.entities.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    List<PaymentEntity> findByReservationId(Long reservationId);
}
