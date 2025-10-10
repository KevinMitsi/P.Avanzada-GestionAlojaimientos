package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.model.ReservationStatus;
import com.avanzada.alojamientos.DTO.other.PaymentDTO;
import com.avanzada.alojamientos.entities.PaymentEntity;
import com.avanzada.alojamientos.entities.ReservationEntity;
import com.avanzada.alojamientos.mappers.PaymentMapper;
import com.avanzada.alojamientos.repositories.PaymentRepository;
import com.avanzada.alojamientos.repositories.ReservationRepository;
import com.avanzada.alojamientos.services.PaymentService;
import lombok.RequiredArgsConstructor;
import com.avanzada.alojamientos.DTO.model.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentDTO register(PaymentDTO dto) {
        // 1) Validar / obtener la reserva referenciada
        ReservationEntity reservation = reservationRepository.findById(dto.reservationId())
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con id=" + dto.reservationId()));

        // 2) Mapear DTO -> Entity (MapStruct) y asegurar la relación con la reserva real
        PaymentEntity payment = paymentMapper.toEntity(dto);
        payment.setReservation(reservation);

        // 3) Si el DTO no traía paidAt, mantenemos null; Pero si status está APPROVED y no hay paidAt, colocar ahora.
        if (payment.getPaidAt() == null && payment.getStatus() == PaymentStatus.COMPLETED) {
            payment.setPaidAt(LocalDateTime.now());
        }

        // 4) Persistir
        PaymentEntity saved = paymentRepository.save(payment);

        // 5) retornar DTO (MapStruct)
        return paymentMapper.toDTO(saved);
    }

    @Override
    public Optional<PaymentDTO> findById(Long id) {
        return paymentRepository.findById(id).map(paymentMapper::toDTO);
    }

    @Override
    public List<PaymentDTO> findByReservation(Long reservationId) {
        List<PaymentEntity> list = paymentRepository.findByReservationId(reservationId);
        return list.stream().map(paymentMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public PaymentDTO confirmPayment(Long paymentId) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con id=" + paymentId));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new RuntimeException("El pago ya está confirmado.");
        }

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());

        // ✅ Actualizar también la reserva asociada
        ReservationEntity reservation = payment.getReservation();
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);

        PaymentEntity updated = paymentRepository.save(payment);
        return paymentMapper.toDTO(updated);
    }
}
