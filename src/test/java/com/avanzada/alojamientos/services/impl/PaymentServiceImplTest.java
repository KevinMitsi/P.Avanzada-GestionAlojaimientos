package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.model.PaymentMethod;
import com.avanzada.alojamientos.DTO.model.PaymentStatus;
import com.avanzada.alojamientos.DTO.model.ReservationStatus;
import com.avanzada.alojamientos.DTO.other.PaymentDTO;
import com.avanzada.alojamientos.entities.PaymentEntity;
import com.avanzada.alojamientos.entities.ReservationEntity;
import com.avanzada.alojamientos.mappers.PaymentMapper;
import com.avanzada.alojamientos.repositories.PaymentRepository;
import com.avanzada.alojamientos.repositories.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private PaymentEntity paymentEntity;
    private PaymentDTO paymentDTO;
    private ReservationEntity reservationEntity;

    @BeforeEach
    void setUp() {
        reservationEntity = new ReservationEntity();
        reservationEntity.setId(1L);
        reservationEntity.setStatus(ReservationStatus.PENDING);
        reservationEntity.setStartDate(LocalDate.now());
        reservationEntity.setEndDate(LocalDate.now().plusDays(2));

        paymentEntity = new PaymentEntity();
        paymentEntity.setId(1L);
        paymentEntity.setAmount(new BigDecimal("250000.50"));
        paymentEntity.setMethod(PaymentMethod.CARD);
        paymentEntity.setStatus(PaymentStatus.PENDING);
        paymentEntity.setReservation(reservationEntity);

        paymentDTO = new PaymentDTO(
                1L,
                1L,
                new BigDecimal("250000.50"),
                PaymentMethod.CARD,
                PaymentStatus.PENDING,
                null
        );
    }

    // ===================== REGISTER =========================

    @Test
    @DisplayName("Debe registrar un pago correctamente (sin paidAt)")
    void register_shouldSavePaymentSuccessfully() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservationEntity));
        when(paymentMapper.toEntity(paymentDTO)).thenReturn(paymentEntity);
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(paymentEntity);
        when(paymentMapper.toDTO(paymentEntity)).thenReturn(paymentDTO);

        PaymentDTO result = paymentService.register(paymentDTO);

        assertNotNull(result);
        assertEquals(paymentDTO.amount(), result.amount());
        verify(paymentRepository, times(1)).save(any(PaymentEntity.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si la reserva no existe")
    void register_shouldThrowIfReservationNotFound() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                paymentService.register(paymentDTO));

        assertTrue(ex.getMessage().contains("Reserva no encontrada"));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe asignar paidAt si status = COMPLETED")
    void register_shouldSetPaidAtWhenCompleted() {
        paymentDTO = new PaymentDTO(1L, 1L, new BigDecimal("100.00"), PaymentMethod.CARD, PaymentStatus.COMPLETED, null);
        paymentEntity.setStatus(PaymentStatus.COMPLETED);
        paymentEntity.setPaidAt(null);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservationEntity));
        when(paymentMapper.toEntity(paymentDTO)).thenReturn(paymentEntity);
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(paymentEntity);
        when(paymentMapper.toDTO(any())).thenReturn(paymentDTO);

        PaymentDTO result = paymentService.register(paymentDTO);

        assertNotNull(result);
        assertNotNull(paymentEntity.getPaidAt());
        verify(paymentRepository, times(1)).save(paymentEntity);
    }

    // ===================== FIND BY ID =========================

    @Test
    @DisplayName("Debe encontrar un pago por ID")
    void findById_shouldReturnPaymentDTO() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(paymentEntity));
        when(paymentMapper.toDTO(paymentEntity)).thenReturn(paymentDTO);

        Optional<PaymentDTO> result = paymentService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(paymentDTO.id(), result.get().id());
        verify(paymentRepository).findById(1L);
    }

    @Test
    @DisplayName("Debe retornar vacío si no existe el pago")
    void findById_shouldReturnEmptyIfNotFound() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<PaymentDTO> result = paymentService.findById(1L);

        assertTrue(result.isEmpty());
    }

    // ===================== FIND BY RESERVATION =========================

    @Test
    @DisplayName("Debe retornar lista de pagos por ID de reserva")
    void findByReservation_shouldReturnList() {
        List<PaymentEntity> list = List.of(paymentEntity);
        when(paymentRepository.findByReservationId(1L)).thenReturn(list);
        when(paymentMapper.toDTO(paymentEntity)).thenReturn(paymentDTO);

        List<PaymentDTO> result = paymentService.findByReservation(1L);

        assertEquals(1, result.size());
        assertEquals(paymentDTO.id(), result.get(0).id());
    }

    @Test
    @DisplayName("Debe retornar lista vacía si no hay pagos")
    void findByReservation_shouldReturnEmptyList() {
        when(paymentRepository.findByReservationId(1L)).thenReturn(Collections.emptyList());

        List<PaymentDTO> result = paymentService.findByReservation(1L);

        assertTrue(result.isEmpty());
    }

    // ===================== CONFIRM PAYMENT =========================

    @Test
    @DisplayName("Debe confirmar un pago correctamente")
    void confirmPayment_shouldConfirmSuccessfully() {
        paymentEntity.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.save(any())).thenReturn(paymentEntity);
        when(paymentMapper.toDTO(any())).thenReturn(paymentDTO);

        PaymentDTO result = paymentService.confirmPayment(1L);

        assertNotNull(result);
        assertEquals(PaymentStatus.COMPLETED, paymentEntity.getStatus());
        assertEquals(ReservationStatus.CONFIRMED, paymentEntity.getReservation().getStatus());
        verify(reservationRepository, times(1)).save(reservationEntity);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el pago no existe")
    void confirmPayment_shouldThrowIfNotFound() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                paymentService.confirmPayment(1L));

        assertTrue(ex.getMessage().contains("Pago no encontrado"));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el pago ya está confirmado")
    void confirmPayment_shouldThrowIfAlreadyCompleted() {
        paymentEntity.setStatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(paymentEntity));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                paymentService.confirmPayment(1L));

        assertTrue(ex.getMessage().contains("ya está confirmado"));
        verify(reservationRepository, never()).save(any());
    }
}
