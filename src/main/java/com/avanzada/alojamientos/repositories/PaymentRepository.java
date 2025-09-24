package com.avanzada.alojamientos.repositories;

import com.avanzada.alojamientos.entities.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
}
