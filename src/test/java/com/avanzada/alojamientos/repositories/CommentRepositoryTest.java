//package com.avanzada.alojamientos.repositories;
//
//import com.avanzada.alojamientos.entities.CommentEntity;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//class CommentRepositoryTest {
//
//    @Autowired
//    private CommentRepository commentRepository;
//
//    @Test
//    void testFindById_ShouldReturnComment() {
//        // Given: ID de comentario existente
//        Long commentId = 1L;
//
//        // When: Buscar por ID
//        Optional<CommentEntity> result = commentRepository.findById(commentId);
//
//        // Then: Debe encontrar el comentario
//        assertTrue(result.isPresent(), "El comentario debe existir");
//        CommentEntity comment = result.get();
//
//        assertEquals(5, comment.getRating(), "El rating debe ser 5");
//        assertNotNull(comment.getUser(), "El usuario debe estar cargado");
//        assertNotNull(comment.getAccommodation(), "El alojamiento debe estar cargado");
//        assertNotNull(comment.getReservation(), "La reservación debe estar cargada");
//    }
//
//    @Test
//    void testFindByAccommodationId() {
//        // Given: ID de alojamiento con comentarios
//        Long accommodationId = 1L;
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // When: Buscar comentarios del alojamiento
//        Page<CommentEntity> result = commentRepository.findByAccommodationId(
//                accommodationId, pageable
//        );
//
//        // Then: Debe encontrar al menos 1 comentario
//        assertFalse(result.isEmpty(), "El alojamiento debe tener comentarios");
//        assertTrue(result.getContent().stream()
//                .allMatch(c -> c.getAccommodation().getId().equals(accommodationId)),
//                "Todos los comentarios deben ser del alojamiento 1");
//    }
//
//    @Test
//    void testFindByAccommodationIdAndIsModeratedFalse() {
//        // Given: ID de alojamiento
//        Long accommodationId = 1L;
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // When: Buscar comentarios no moderados
//        Page<CommentEntity> result = commentRepository.findByAccommodationIdAndIsModeratedFalse(
//                accommodationId, pageable
//        );
//
//        // Then: Debe encontrar comentarios no moderados
//        assertFalse(result.isEmpty(), "Debe haber comentarios no moderados");
//        assertTrue(result.getContent().stream()
//                .noneMatch(CommentEntity::getIsModerated),
//                "Todos deben estar sin moderar");
//    }
//
//    @Test
//    void testFindByUserId() {
//        // Given: ID de usuario con comentarios
//        Long userId = 5L; // Laura Pérez
//
//        // When: Buscar comentarios del usuario
//        List<CommentEntity> result = commentRepository.findByUserId(userId);
//
//        // Then: Debe encontrar al menos 1 comentario
//        assertFalse(result.isEmpty(), "Laura debe tener al menos un comentario");
//        assertTrue(result.stream()
//                .allMatch(c -> c.getUser().getId().equals(userId)),
//                "Todos los comentarios deben ser de Laura");
//    }
//
//    @Test
//    void testCountByAccommodationId() {
//        // Given: ID de alojamiento con comentarios
//        Long accommodationId = 1L;
//
//        // When: Contar comentarios
//        long count = commentRepository.countByAccommodationId(accommodationId);
//
//        // Then: Debe tener al menos 1 comentario
//        assertTrue(count >= 1, "El alojamiento debe tener al menos 1 comentario");
//    }
//
//    @Test
//    void testCalculateAverageRating() {
//        // Given: ID de alojamiento con comentarios
//        Long accommodationId = 1L;
//
//        // When: Calcular promedio de rating
//        Double average = commentRepository.calculateAverageRating(accommodationId);
//
//        // Then: Debe retornar un promedio válido
//        assertNotNull(average, "Debe retornar un promedio");
//        assertTrue(average >= 1.0 && average <= 5.0, "El promedio debe estar entre 1 y 5");
//    }
//
//    @Test
//    void testFindByReservationId() {
//        // Given: ID de reservación con comentario
//        Long reservationId = 1L;
//
//        // When: Buscar comentario por reservación
//        Optional<CommentEntity> result = commentRepository.findByReservationId(reservationId);
//
//        // Then: Debe encontrar el comentario
//        assertTrue(result.isPresent(), "La reservación debe tener un comentario");
//        assertEquals(reservationId, result.get().getReservation().getId());
//    }
//
//    @Test
//    void testExistsByReservationId() {
//        // Given: ID de reservación con comentario
//        Long reservationId = 1L;
//
//        // When: Verificar existencia
//        boolean exists = commentRepository.existsByReservationId(reservationId);
//
//        // Then: Debe retornar true
//        assertTrue(exists, "La reservación debe tener un comentario");
//    }
//
//    @Test
//    void testExistsByReservationId_WithoutComment() {
//        // Given: ID de reservación sin comentario
//        Long reservationId = 3L;
//
//        // When: Verificar existencia
//        boolean exists = commentRepository.existsByReservationId(reservationId);
//
//        // Then: Debe retornar false
//        assertFalse(exists, "La reservación no debe tener comentario");
//    }
//
//    @Test
//    void testCommentWithReply() {
//        // Given: ID de comentario con respuesta del host
//        Long commentId = 1L;
//
//        // When: Buscar el comentario
//        Optional<CommentEntity> result = commentRepository.findById(commentId);
//
//        // Then: Debe tener respuesta
//        assertTrue(result.isPresent(), "El comentario debe existir");
//        assertNotNull(result.get().getReply(), "Debe tener respuesta del host");
//        assertNotNull(result.get().getReplyAt(), "Debe tener fecha de respuesta");
//    }
//
//    @Test
//    void testFindTopRatedComments() {
//        // Given: Buscar comentarios con rating alto
//        Pageable pageable = PageRequest.of(0, 5);
//
//        // When: Buscar todos los comentarios ordenados por rating
//        Page<CommentEntity> result = commentRepository.findByAccommodationId(1L, pageable);
//
//        // Then: Verificar que hay comentarios
//        assertFalse(result.isEmpty(), "Debe haber comentarios");
//    }
//
//    @Test
//    void testCommentText() {
//        // Given: ID de comentario con texto específico
//        Long commentId = 1L;
//
//        // When: Buscar el comentario
//        Optional<CommentEntity> result = commentRepository.findById(commentId);
//
//        // Then: Verificar el texto
//        assertTrue(result.isPresent(), "El comentario debe existir");
//        assertNotNull(result.get().getCommentText(), "Debe tener texto");
//        assertTrue(result.get().getCommentText().length() > 10,
//                "El texto debe tener contenido significativo");
//    }
//
//    @Test
//    void testFindByAccommodationHostId() {
//        // Given: ID del host
//        Long hostId = 2L; // María González
//
//        // When: Buscar comentarios en alojamientos del host
//        List<CommentEntity> result = commentRepository.findByAccommodationHostId(hostId);
//
//        // Then: Debe encontrar comentarios
//        assertFalse(result.isEmpty(), "El host debe tener comentarios en sus alojamientos");
//        assertTrue(result.stream()
//                .allMatch(c -> c.getAccommodation().getHost().getId().equals(hostId)),
//                "Todos los comentarios deben ser de alojamientos del host");
//    }
//}
//
