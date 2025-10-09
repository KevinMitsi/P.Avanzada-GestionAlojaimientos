package com.avanzada.alojamientos.mappers;

import com.avanzada.alojamientos.DTO.comment.CommentDTO;
import com.avanzada.alojamientos.DTO.comment.CreateCommentDTO;
import com.avanzada.alojamientos.DTO.host.ReplyHostDTO;
import com.avanzada.alojamientos.DTO.model.HostReply;
import com.avanzada.alojamientos.entities.AccommodationEntity;
import com.avanzada.alojamientos.entities.CommentEntity;
import com.avanzada.alojamientos.entities.ReservationEntity;
import com.avanzada.alojamientos.entities.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CommentMapperTest {

    @Autowired
    private CommentMapper commentMapper;

    private CommentEntity commentEntity;
    private CreateCommentDTO createCommentDTO;
    private HostReply hostReply;

    @BeforeEach
    void setUp() {
        // Setup User Entity
        UserEntity userEntity = new UserEntity();
        userEntity.setId(100L);
        userEntity.setName("Juan Pérez");
        userEntity.setEmail("juan@example.com");

        // Setup Accommodation Entity
        AccommodationEntity accommodationEntity = new AccommodationEntity();
        accommodationEntity.setId(50L);
        accommodationEntity.setTitle("Apartamento Centro");

        // Setup Reservation Entity
        ReservationEntity reservationEntity = new ReservationEntity();
        reservationEntity.setId(10L);

        // Setup HostReply
        hostReply = new HostReply();
        hostReply.setHostId(200L);
        hostReply.setReply("Gracias por tu comentario");
        hostReply.setReplyAt(LocalDateTime.of(2025, 10, 8, 15, 30));

        // Setup Comment Entity
        commentEntity = new CommentEntity();
        commentEntity.setId(1L);
        commentEntity.setRating(5);
        commentEntity.setText("Excelente alojamiento, muy recomendado");
        commentEntity.setCreatedAt(LocalDateTime.of(2025, 10, 5, 10, 0));
        commentEntity.setIsModerated(true);
        commentEntity.setUser(userEntity);
        commentEntity.setAccommodation(accommodationEntity);
        commentEntity.setReservation(reservationEntity);
        commentEntity.setHostReply(hostReply);

        // Setup CreateCommentDTO
        createCommentDTO = new CreateCommentDTO(
                4,
                "Muy buen lugar, limpio y cómodo"
        );
    }

    @Test
    @DisplayName("Debe mapear CommentEntity a CommentDTO correctamente")
    void toCommentDTO_shouldMapCorrectly() {
        // When
        CommentDTO dto = commentMapper.toCommentDTO(commentEntity);

        // Then
        assertNotNull(dto);
        assertEquals(commentEntity.getId(), dto.id());
        assertEquals(commentEntity.getRating().floatValue(), dto.rating());
        assertEquals(commentEntity.getText(), dto.text());
        assertEquals(commentEntity.getCreatedAt(), dto.createdAt());
        assertEquals(commentEntity.getIsModerated(), dto.isModerated());
        assertEquals(commentEntity.getReservation().getId(), dto.reservationId());
        assertEquals(commentEntity.getAccommodation().getId().intValue(), dto.accommodationId());
        assertEquals(commentEntity.getUser().getId(), dto.userId());

        // Verificar hostReply
        assertNotNull(dto.hostReply());
        assertEquals(hostReply.getHostId(), dto.hostReply().hostId());
        assertEquals(hostReply.getReply(), dto.hostReply().text());
        assertEquals(hostReply.getReplyAt(), dto.hostReply().createdAt());
    }

    @Test
    @DisplayName("Debe retornar null cuando CommentEntity es null")
    void toCommentDTO_withNullEntity_shouldReturnNull() {
        // When
        CommentDTO dto = commentMapper.toCommentDTO(null);

        // Then
        assertNull(dto);
    }

    @Test
    @DisplayName("Debe mapear CommentEntity sin hostReply correctamente")
    void toCommentDTO_withoutHostReply_shouldMapCorrectly() {
        // Given
        commentEntity.setHostReply(null);

        // When
        CommentDTO dto = commentMapper.toCommentDTO(commentEntity);

        // Then
        assertNotNull(dto);
        assertNull(dto.hostReply());
        assertEquals(commentEntity.getRating().floatValue(), dto.rating());
        assertEquals(commentEntity.getText(), dto.text());
    }

    @Test
    @DisplayName("Debe mapear CreateCommentDTO a CommentEntity correctamente")
    void toEntity_shouldMapCorrectly() {
        // When
        CommentEntity entity = commentMapper.toEntity(createCommentDTO);

        // Then
        assertNotNull(entity);
        assertNull(entity.getId()); // ID debe ser null (se asigna en la BD)
        assertEquals(createCommentDTO.rating(), entity.getRating());
        assertEquals(createCommentDTO.text(), entity.getText());

        // Verificar valores por defecto
        assertFalse(entity.getIsModerated());
        assertNotNull(entity.getCreatedAt()); // Se genera automáticamente

        // Verificar campos ignorados (se asignan en el servicio)
        assertNull(entity.getReservation());
        assertNull(entity.getAccommodation());
        assertNull(entity.getUser());
        assertNull(entity.getHostReply());
    }

    @Test
    @DisplayName("Debe retornar null cuando CreateCommentDTO es null")
    void toEntity_withNullDTO_shouldReturnNull() {
        // When
        CommentEntity entity = commentMapper.toEntity(null);

        // Then
        assertNull(entity);
    }

    @Test
    @DisplayName("Debe generar createdAt automáticamente al crear entidad")
    void toEntity_shouldGenerateCreatedAt() {
        // Given
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        // When
        CommentEntity entity = commentMapper.toEntity(createCommentDTO);

        // Then
        assertNotNull(entity.getCreatedAt());
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        assertTrue(entity.getCreatedAt().isAfter(before));
        assertTrue(entity.getCreatedAt().isBefore(after));
    }

    @Test
    @DisplayName("Debe establecer isModerated en false por defecto")
    void toEntity_shouldSetIsModeratedToFalse() {
        // When
        CommentEntity entity = commentMapper.toEntity(createCommentDTO);

        // Then
        assertNotNull(entity.getIsModerated());
        assertFalse(entity.getIsModerated());
    }

    @Test
    @DisplayName("Debe mapear HostReply a ReplyHostDTO correctamente")
    void toReplyHostDTO_shouldMapCorrectly() {
        // When
        ReplyHostDTO dto = commentMapper.toReplyHostDTO(hostReply);

        // Then
        assertNotNull(dto);
        assertEquals(hostReply.getHostId(), dto.hostId());
        assertEquals(hostReply.getReply(), dto.text());
        assertEquals(hostReply.getReplyAt(), dto.createdAt());
    }

    @Test
    @DisplayName("Debe retornar null cuando HostReply es null")
    void toReplyHostDTO_withNullHostReply_shouldReturnNull() {
        // When
        ReplyHostDTO dto = commentMapper.toReplyHostDTO(null);

        // Then
        assertNull(dto);
    }

    @Test
    @DisplayName("Debe manejar diferentes ratings correctamente")
    void toCommentDTO_withDifferentRatings_shouldMapCorrectly() {
        // Given - Probar con diferentes valores de rating
        for (int rating = 1; rating <= 5; rating++) {
            commentEntity.setRating(rating);

            // When
            CommentDTO dto = commentMapper.toCommentDTO(commentEntity);

            // Then
            assertNotNull(dto);
            assertEquals(rating, dto.rating().intValue());
        }
    }

    @Test
    @DisplayName("Debe manejar textos largos correctamente")
    void toEntity_withLongText_shouldMapCorrectly() {
        // Given
        String longText = "a".repeat(500); // Máximo permitido
        CreateCommentDTO dtoWithLongText = new CreateCommentDTO(3, longText);

        // When
        CommentEntity entity = commentMapper.toEntity(dtoWithLongText);

        // Then
        assertNotNull(entity);
        assertEquals(longText, entity.getText());
        assertEquals(500, entity.getText().length());
    }

    @Test
    @DisplayName("Debe manejar comentarios no moderados correctamente")
    void toCommentDTO_withNonModeratedComment_shouldMapCorrectly() {
        // Given
        commentEntity.setIsModerated(false);

        // When
        CommentDTO dto = commentMapper.toCommentDTO(commentEntity);

        // Then
        assertNotNull(dto);
        assertFalse(dto.isModerated());
    }

    @Test
    @DisplayName("Debe mapear correctamente las relaciones con IDs de diferentes tipos")
    void toCommentDTO_shouldMapDifferentIdTypes() {
        // Given - Verificar que los tipos de ID son diferentes
        // reservationId: Integer
        // accommodationId: Long -> Integer
        // userId: Long

        // When
        CommentDTO dto = commentMapper.toCommentDTO(commentEntity);

        // Then
        assertNotNull(dto);
        // Verificar que los tipos se convierten correctamente
        assertInstanceOf(Long.class, dto.reservationId());
        assertInstanceOf(Long.class, dto.accommodationId());
        assertInstanceOf(Long.class, dto.userId());

        assertEquals(10, dto.reservationId());
        assertEquals(50, dto.accommodationId());
        assertEquals(100L, dto.userId());
    }

    @Test
    @DisplayName("Debe manejar HostReply con diferentes fechas correctamente")
    void toReplyHostDTO_withDifferentDates_shouldMapCorrectly() {
        // Given
        LocalDateTime pastDate = LocalDateTime.of(2020, 1, 1, 0, 0);
        LocalDateTime futureDate = LocalDateTime.of(2030, 12, 31, 23, 59);

        // Test con fecha pasada
        hostReply.setReplyAt(pastDate);
        ReplyHostDTO dto1 = commentMapper.toReplyHostDTO(hostReply);
        assertEquals(pastDate, dto1.createdAt());

        // Test con fecha futura
        hostReply.setReplyAt(futureDate);
        ReplyHostDTO dto2 = commentMapper.toReplyHostDTO(hostReply);
        assertEquals(futureDate, dto2.createdAt());
    }

    @Test
    @DisplayName("Debe preservar el rating exacto al mapear")
    void toEntity_shouldPreserveRatingValue() {
        // Given - Crear DTOs con diferentes ratings
        CreateCommentDTO dto1 = new CreateCommentDTO(1, "Malo");
        CreateCommentDTO dto5 = new CreateCommentDTO(5, "Excelente");

        // When
        CommentEntity entity1 = commentMapper.toEntity(dto1);
        CommentEntity entity5 = commentMapper.toEntity(dto5);

        // Then
        assertEquals(1, entity1.getRating());
        assertEquals(5, entity5.getRating());
    }
}