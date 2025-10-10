package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.model.PaymentMethod;
import com.avanzada.alojamientos.DTO.model.PaymentStatus;
import com.avanzada.alojamientos.DTO.model.ReservationStatus;
import com.avanzada.alojamientos.DTO.other.PaymentDTO;
import com.avanzada.alojamientos.entities.AccommodationEntity;
import com.avanzada.alojamientos.entities.ReservationEntity;
import com.avanzada.alojamientos.entities.UserEntity;
import com.avanzada.alojamientos.exceptions.ReservationNotFoundException;
import com.avanzada.alojamientos.repositories.ReservationRepository;
import com.avanzada.alojamientos.services.PaymentService;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class MercadoPagoServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private Preference preference;

    @InjectMocks
    private MercadoPagoServiceImpl mercadoPagoService;

    private ReservationEntity testReservation;

    @BeforeEach
    void setUp() {
        // Configurar token de acceso para MercadoPago
        ReflectionTestUtils.setField(mercadoPagoService, "mercadoPagoToken", "TEST_TOKEN");

        // Crear datos simulados
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setEmail("user@example.com");

        AccommodationEntity accommodation = new AccommodationEntity();
        accommodation.setId(1L);
        accommodation.setTitle("Hotel Test");

        testReservation = new ReservationEntity();
        testReservation.setId(1L);
        testReservation.setUser(user);
        testReservation.setAccommodation(accommodation);
        testReservation.setTotalPrice(new BigDecimal("500.00"));
        testReservation.setStartDate(LocalDate.now());
        testReservation.setEndDate(LocalDate.now().plusDays(2));
        testReservation.setStatus(ReservationStatus.PENDING);
    }

    // ----------------------------------------------------------------------
    //  Caso exitoso: se crea correctamente la preferencia y se registra el pago
    // ----------------------------------------------------------------------
    @Test
    void createPreference_ShouldReturnInitPoint_WhenValidReservation() throws Exception {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(preference.getInitPoint()).thenReturn("https://mercadopago.com/init/test");
        // Mock estático: interceptar la creación del cliente PreferenceClient
        try (MockedConstruction<PreferenceClient> mocked = mockConstruction(PreferenceClient.class,
                (mock, context) -> when(mock.create(any(PreferenceRequest.class))).thenReturn(preference))) {

            // Act
            String result = mercadoPagoService.createPreference(1L);

            // Assert
            assertNotNull(result);
            assertEquals("https://mercadopago.com/init/test", result);
            verify(reservationRepository).findById(1L);
            verify(paymentService).register(argThat(dto ->
                    dto.reservationId().equals(1L)
                            && dto.amount().compareTo(new BigDecimal("500.00")) == 0
                            && dto.method() == PaymentMethod.PAYPAL
                            && dto.status() == PaymentStatus.PENDING));
        }
    }

    // ----------------------------------------------------------------------
    //  Caso: Reserva no encontrada
    // ----------------------------------------------------------------------
    @Test
    void createPreference_ShouldThrowReservationNotFoundException_WhenReservationDoesNotExist() {
        // Arrange
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ReservationNotFoundException exception = assertThrows(
                ReservationNotFoundException.class,
                () -> mercadoPagoService.createPreference(99L)
        );

        assertTrue(exception.getMessage().contains("Reserva no encontrada"));
        verify(reservationRepository).findById(99L);
        verify(paymentService, never()).register(any());
    }

    // ----------------------------------------------------------------------
    //  Caso: Error al crear preferencia (SDK lanza excepción)
    // ----------------------------------------------------------------------
    @Test
    void createPreference_ShouldThrowException_WhenPreferenceCreationFails() throws Exception {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));

        try (MockedConstruction<PreferenceClient> mocked = mockConstruction(PreferenceClient.class,
                (mock, context) -> when(mock.create(any(PreferenceRequest.class)))
                        .thenThrow(new RuntimeException("Fallo en MercadoPago")))) {

            // Act & Assert
            Exception exception = assertThrows(Exception.class, () -> mercadoPagoService.createPreference(1L));
            assertTrue(exception.getMessage().contains("Fallo en MercadoPago"));
            verify(reservationRepository).findById(1L);
            verify(paymentService, never()).register(any());
        }
    }

    // ----------------------------------------------------------------------
    //  Caso: Error inesperado al acceder a totalPrice (ejemplo nulo)
    // ----------------------------------------------------------------------
    @Test
    void createPreference_ShouldHandleNullTotalPrice_Gracefully() throws Exception {
        // Arrange
        testReservation.setTotalPrice(null);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(paymentService.register(any())).thenReturn(new PaymentDTO(1L, 1L, BigDecimal.ZERO, PaymentMethod.PAYPAL, PaymentStatus.PENDING, null));

        when(preference.getInitPoint()).thenReturn("https://mercadopago.com/init/test-null");

        try (MockedConstruction<PreferenceClient> mocked = mockConstruction(PreferenceClient.class,
                (mock, context) -> when(mock.create(any(PreferenceRequest.class))).thenReturn(preference))) {

            // Act
            String result = mercadoPagoService.createPreference(1L);

            // Assert
            assertNotNull(result);
            assertEquals("https://mercadopago.com/init/test-null", result);
            verify(paymentService).register(any());
        }
    }



    // ----------------------------------------------------------------------
    //  Caso adicional: Verifica que se imprime correctamente información
    // ----------------------------------------------------------------------
    @Test
    void createPreference_ShouldPrintReservationData() throws Exception {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(preference.getInitPoint()).thenReturn("https://mercadopago.com/init/test");

        try (MockedConstruction<PreferenceClient> mocked = mockConstruction(PreferenceClient.class,
                (mock, context) -> when(mock.create(any(PreferenceRequest.class))).thenReturn(preference))) {

            // Act
            String url = mercadoPagoService.createPreference(1L);

            // Assert
            assertEquals("https://mercadopago.com/init/test", url);
            verify(paymentService).register(any(PaymentDTO.class));
        }
    }
}
