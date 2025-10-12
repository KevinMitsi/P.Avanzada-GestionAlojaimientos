package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.comment.CommentDTO;
import com.avanzada.alojamientos.DTO.comment.CreateCommentDTO;
import com.avanzada.alojamientos.DTO.model.HostReply;
import com.avanzada.alojamientos.DTO.model.ReservationStatus;
import com.avanzada.alojamientos.entities.*;
import com.avanzada.alojamientos.exceptions.AccommodationNotFoundException;
import com.avanzada.alojamientos.exceptions.CommentForbiddenException;
import com.avanzada.alojamientos.exceptions.CommentNotFoundException;
import com.avanzada.alojamientos.exceptions.UnauthorizedException;
import com.avanzada.alojamientos.exceptions.UserNotFoundException;
import com.avanzada.alojamientos.mappers.CommentMapper;
import com.avanzada.alojamientos.repositories.AccommodationRepository;
import com.avanzada.alojamientos.repositories.CommentRepository;
import com.avanzada.alojamientos.repositories.ReservationRepository;
import com.avanzada.alojamientos.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentServiceImpl commentService;

    private UserEntity testUser;
    private UserEntity testHost;
    private AccommodationEntity testAccommodation;
    private ReservationEntity testReservation;
    private CommentEntity testComment;
    private CommentDTO testCommentDTO;
    private CreateCommentDTO createCommentDTO;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new UserEntity();
        testUser.setId(1L);
        testUser.setEmail("guest@example.com");
        testUser.setName("Test Guest");

        // Setup test host
        testHost = new UserEntity();
        testHost.setId(2L);
        testHost.setEmail("host@example.com");
        testHost.setName("Test Host");

        // Setup test accommodation
        testAccommodation = new AccommodationEntity();
        testAccommodation.setId(1L);
        testAccommodation.setTitle("Test Accommodation");
        testAccommodation.setHost(testHost);

        // Setup test reservation (completed)
        testReservation = new ReservationEntity();
        testReservation.setId(1L);
        testReservation.setUser(testUser);
        testReservation.setAccommodation(testAccommodation);
        testReservation.setStartDate(LocalDate.now().minusDays(10));
        testReservation.setEndDate(LocalDate.now().minusDays(5));
        testReservation.setStatus(ReservationStatus.CONFIRMED);

        // Setup CreateCommentDTO
        createCommentDTO = new CreateCommentDTO(5, "Great place!");

        // Setup test comment entity
        testComment = new CommentEntity();
        testComment.setId(1L);
        testComment.setRating(5);
        testComment.setText("Great place!");
        testComment.setCreatedAt(LocalDateTime.now());
        testComment.setIsModerated(false);
        testComment.setReservation(testReservation);
        testComment.setAccommodation(testAccommodation);
        testComment.setUser(testUser);
        HostReply hostReply = new HostReply();
        hostReply.setHostId(testHost.getId());
        testComment.setHostReply(hostReply);

        // Setup test comment DTO
        testCommentDTO = new CommentDTO(
                1L,
                5.0f,
                "Great place!",
                LocalDateTime.now(),
                false,
                1L,
                1L,
                1L,
                null
        );
    }

    // CREATE TESTS
    @Test
    void create_Success() {
        // Arrange
        Long userId = 1L;
        Long reservationId = 1L;
        Long accommodationId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(testReservation));
        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(testAccommodation));
        when(commentRepository.existsByReservation_Id(reservationId)).thenReturn(false);
        when(commentMapper.toEntity(createCommentDTO)).thenReturn(testComment);
        when(commentRepository.save(any(CommentEntity.class))).thenReturn(testComment);
        when(commentMapper.toCommentDTO(testComment)).thenReturn(testCommentDTO);

        // Act
        CommentDTO result = commentService.create(userId, reservationId, createCommentDTO, accommodationId);

        // Assert
        assertNotNull(result);
        assertEquals(testCommentDTO, result);
        verify(userRepository).findById(userId);
        verify(reservationRepository).findById(reservationId);
        verify(accommodationRepository).findById(accommodationId);
        verify(commentRepository).existsByReservation_Id(reservationId);
        verify(commentRepository).save(any(CommentEntity.class));
        verify(commentMapper).toCommentDTO(testComment);
    }

    @Test
    void create_UserNotFound_ThrowsUserNotFoundException() {
        // Arrange
        Long userId = 1L;
        Long reservationId = 1L;
        Long accommodationId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> commentService.create(userId, reservationId, createCommentDTO, accommodationId)
        );

        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository).findById(userId);
        verify(reservationRepository, never()).findById(any());
    }

    @Test
    void create_ReservationNotFound_ThrowsUserNotFoundException() {
        // Arrange
        Long userId = 1L;
        Long reservationId = 1L;
        Long accommodationId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> commentService.create(userId, reservationId, createCommentDTO, accommodationId)
        );

        assertTrue(exception.getMessage().contains("Reservation not found"));
        verify(userRepository).findById(userId);
        verify(reservationRepository).findById(reservationId);
    }

    @Test
    void create_AccommodationNotFound_ThrowsUserNotFoundException() {
        // Arrange
        Long userId = 1L;
        Long reservationId = 1L;
        Long accommodationId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(testReservation));
        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> commentService.create(userId, reservationId, createCommentDTO, accommodationId)
        );

        assertTrue(exception.getMessage().contains("Accommodation not found"));
        verify(userRepository).findById(userId);
        verify(reservationRepository).findById(reservationId);
        verify(accommodationRepository).findById(accommodationId);
    }

    @Test
    void create_ReservationNotBelongsToUser_ThrowsCommentForbiddenException() {
        // Arrange
        Long userId = 999L; // Different user
        Long reservationId = 1L;
        Long accommodationId = 1L;

        UserEntity differentUser = new UserEntity();
        differentUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(differentUser));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(testReservation));
        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(testAccommodation));

        // Act & Assert
        CommentForbiddenException exception = assertThrows(
                CommentForbiddenException.class,
                () -> commentService.create(userId, reservationId, createCommentDTO, accommodationId)
        );

        assertTrue(exception.getMessage().contains("User cannot comment on a reservation they did not make"));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void create_ReservationNotForSpecifiedAccommodation_ThrowsCommentForbiddenException() {
        // Arrange
        Long userId = 1L;
        Long reservationId = 1L;
        Long accommodationId = 999L; // Different accommodation

        AccommodationEntity differentAccommodation = new AccommodationEntity();
        differentAccommodation.setId(accommodationId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(testReservation));
        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(differentAccommodation));

        // Act & Assert
        CommentForbiddenException exception = assertThrows(
                CommentForbiddenException.class,
                () -> commentService.create(userId, reservationId, createCommentDTO, accommodationId)
        );

        assertTrue(exception.getMessage().contains("Reservation is not for the specified accommodation"));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void create_ReservationNotCompleted_ThrowsCommentForbiddenException() {
        // Arrange
        Long userId = 1L;
        Long reservationId = 1L;
        Long accommodationId = 1L;

        // Set reservation end date in the future
        testReservation.setEndDate(LocalDate.now().plusDays(5));

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(testReservation));
        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(testAccommodation));

        // Act & Assert
        CommentForbiddenException exception = assertThrows(
                CommentForbiddenException.class,
                () -> commentService.create(userId, reservationId, createCommentDTO, accommodationId)
        );

        assertTrue(exception.getMessage().contains("Cannot comment on a reservation that hasn't been completed"));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void create_UserAlreadyCommented_ThrowsCommentForbiddenException() {
        // Arrange
        Long userId = 1L;
        Long reservationId = 1L;
        Long accommodationId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(testReservation));
        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(testAccommodation));
        when(commentRepository.existsByReservation_Id(reservationId)).thenReturn(true);

        // Act & Assert
        CommentForbiddenException exception = assertThrows(
                CommentForbiddenException.class,
                () -> commentService.create(userId, reservationId, createCommentDTO, accommodationId)
        );

        assertTrue(exception.getMessage().contains("User has already commented on this reservation"));
        verify(commentRepository, never()).save(any());
    }

    // FIND BY ID TESTS
    @Test
    void findById_Success() {
        // Arrange
        Long commentId = 1L;

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(commentMapper.toCommentDTO(testComment)).thenReturn(testCommentDTO);

        // Act
        CommentDTO result = commentService.findById(commentId);

        // Assert
        assertNotNull(result);
        assertEquals(testCommentDTO, result);
        verify(commentRepository).findById(commentId);
        verify(commentMapper).toCommentDTO(testComment);
    }

    @Test
    void findById_CommentNotFound_ThrowsCommentNotFoundException() {
        // Arrange
        Long commentId = 1L;

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // Act & Assert
        CommentNotFoundException exception = assertThrows(
                CommentNotFoundException.class,
                () -> commentService.findById(commentId)
        );

        assertTrue(exception.getMessage().contains("Comment not found"));
        assertTrue(exception.getMessage().contains(commentId.toString()));
        verify(commentRepository).findById(commentId);
        verify(commentMapper, never()).toCommentDTO(any());
    }

    // FIND BY ACCOMMODATION TESTS
    @Test
    void findByAccommodation_Success() {
        // Arrange
        Long accommodationId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<CommentEntity> commentPage = new PageImpl<>(List.of(testComment));

        when(accommodationRepository.existsById(accommodationId)).thenReturn(true);
        when(commentRepository.findAllByAccommodation_Id(accommodationId, pageable)).thenReturn(commentPage);
        when(commentMapper.toCommentDTO(testComment)).thenReturn(testCommentDTO);

        // Act
        Page<CommentDTO> result = commentService.findByAccommodation(accommodationId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testCommentDTO, result.getContent().getFirst());
        verify(accommodationRepository).existsById(accommodationId);
        verify(commentRepository).findAllByAccommodation_Id(accommodationId, pageable);
    }

    @Test
    void findByAccommodation_AccommodationNotFound_ThrowsAccommodationNotFoundException() {
        // Arrange
        Long accommodationId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        when(accommodationRepository.existsById(accommodationId)).thenReturn(false);

        // Act & Assert
        AccommodationNotFoundException exception = assertThrows(
                AccommodationNotFoundException.class,
                () -> commentService.findByAccommodation(accommodationId, pageable)
        );

        assertTrue(exception.getMessage().contains("Accommodation not found"));
        assertTrue(exception.getMessage().contains(accommodationId.toString()));
        verify(accommodationRepository).existsById(accommodationId);
        verify(commentRepository, never()).findAllByAccommodation_Id(any(), any());
    }

    @Test
    void findByAccommodation_NoComments_ReturnsEmptyPage() {
        // Arrange
        Long accommodationId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<CommentEntity> emptyPage = new PageImpl<>(Collections.emptyList());

        when(accommodationRepository.existsById(accommodationId)).thenReturn(true);
        when(commentRepository.findAllByAccommodation_Id(accommodationId, pageable)).thenReturn(emptyPage);

        // Act
        Page<CommentDTO> result = commentService.findByAccommodation(accommodationId, pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        verify(accommodationRepository).existsById(accommodationId);
        verify(commentRepository).findAllByAccommodation_Id(accommodationId, pageable);
    }

    // FIND BY USER TESTS
    @Test
    void findByUser_Success() {
        // Arrange
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<CommentEntity> commentPage = new PageImpl<>(List.of(testComment));

        when(userRepository.existsById(userId)).thenReturn(true);
        when(commentRepository.findAllByUser_Id(userId, pageable)).thenReturn(commentPage);
        when(commentMapper.toCommentDTO(testComment)).thenReturn(testCommentDTO);

        // Act
        Page<CommentDTO> result = commentService.findByUser(userId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testCommentDTO, result.getContent().getFirst());
        verify(userRepository).existsById(userId);
        verify(commentRepository).findAllByUser_Id(userId, pageable);
    }

    @Test
    void findByUser_UserNotFound_ThrowsUserNotFoundException() {
        // Arrange
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.existsById(userId)).thenReturn(false);

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> commentService.findByUser(userId, pageable)
        );

        assertTrue(exception.getMessage().contains("User not found"));
        assertTrue(exception.getMessage().contains(userId.toString()));
        verify(userRepository).existsById(userId);
        verify(commentRepository, never()).findAllByUser_Id(any(), any());
    }

    @Test
    void findByUser_NoComments_ReturnsEmptyPage() {
        // Arrange
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<CommentEntity> emptyPage = new PageImpl<>(Collections.emptyList());

        when(userRepository.existsById(userId)).thenReturn(true);
        when(commentRepository.findAllByUser_Id(userId, pageable)).thenReturn(emptyPage);

        // Act
        Page<CommentDTO> result = commentService.findByUser(userId, pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        verify(userRepository).existsById(userId);
        verify(commentRepository).findAllByUser_Id(userId, pageable);
    }

    // REPLY TESTS
    @Test
    void reply_Success() {
        // Arrange
        Long commentId = 1L;
        Long hostId = 2L;
        String replyText = "Thank you for your feedback!";

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(CommentEntity.class))).thenReturn(testComment);

        // Act
        commentService.reply(commentId, hostId, replyText);

        // Assert
        verify(commentRepository).findById(commentId);
        verify(commentRepository).save(argThat(comment -> {
            HostReply hostReply = comment.getHostReply();
            return hostReply != null &&
                   hostReply.getHostId().equals(hostId) &&
                   hostReply.getReply().equals(replyText) &&
                   hostReply.getReplyAt() != null;
        }));
    }

    @Test
    void reply_CommentNotFound_ThrowsCommentNotFoundException() {
        // Arrange
        Long commentId = 1L;
        Long hostId = 2L;
        String replyText = "Thank you!";

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // Act & Assert
        CommentNotFoundException exception = assertThrows(
                CommentNotFoundException.class,
                () -> commentService.reply(commentId, hostId, replyText)
        );

        assertTrue(exception.getMessage().contains("Comment not found"));
        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void reply_HostDoesNotOwnAccommodation_ThrowsCommentForbiddenException() {
        // Arrange
        Long commentId = 1L;
        Long wrongHostId = 999L; // Different host
        String replyText = "Thank you!";

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));

        // Act & Assert
        CommentForbiddenException exception = assertThrows(
                CommentForbiddenException.class,
                () -> commentService.reply(commentId, wrongHostId, replyText)
        );

        assertTrue(exception.getMessage().contains("Only the host of the accommodation can reply"));
        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).save(any());
    }

    // MODERATE TESTS
    @Test
    void moderate_ApproveComment_Success() {
        // Arrange
        Long commentId = 1L;
        Long hostId = 2L;
        boolean approved = true;

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(CommentEntity.class))).thenReturn(testComment);

        // Act
        commentService.moderate(commentId, hostId, approved);

        // Assert
        verify(commentRepository).findById(commentId);
        verify(commentRepository).save(argThat(comment -> comment.getIsModerated().equals(true)));
        verify(commentRepository, never()).deleteById(any());
    }

    @Test
    void moderate_RejectComment_DeletesComment() {
        // Arrange
        Long commentId = 1L;
        Long hostId = 2L;
        boolean approved = false;

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));

        // Act
        commentService.moderate(commentId, hostId, approved);

        // Assert
        verify(commentRepository).findById(commentId);
        verify(commentRepository).deleteById(commentId);
    }

    @Test
    void moderate_CommentNotFound_ThrowsCommentNotFoundException() {
        // Arrange
        Long commentId = 1L;
        Long hostId = 2L;
        boolean approved = true;

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // Act & Assert
        CommentNotFoundException exception = assertThrows(
                CommentNotFoundException.class,
                () -> commentService.moderate(commentId, hostId, approved)
        );

        assertTrue(exception.getMessage().contains("Comment not found"));
        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).save(any());
        verify(commentRepository, never()).deleteById(any());
    }

    @Test
    void moderate_HostDoesNotOwnAccommodation_ThrowsCommentForbiddenException() {
        // Arrange
        Long commentId = 1L;
        Long wrongHostId = 999L; // Different host
        boolean approved = true;

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));

        // Act & Assert
        CommentForbiddenException exception = assertThrows(
                CommentForbiddenException.class,
                () -> commentService.moderate(commentId, wrongHostId, approved)
        );

        assertTrue(exception.getMessage().contains("Only the host of the accommodation can moderate"));
        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).save(any());
        verify(commentRepository, never()).deleteById(any());
    }

    // DELETE TESTS
    @Test
    void delete_Success() {
        // Arrange
        Long userId = 1L;
        Long commentId = 1L;

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));

        // Act
        commentService.delete(userId, commentId);

        // Assert
        verify(commentRepository).findById(commentId);
        verify(commentRepository).deleteComment(commentId);
    }

    @Test
    void delete_CommentNotFound_ThrowsCommentNotFoundException() {
        // Arrange
        Long userId = 1L;
        Long commentId = 1L;

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // Act & Assert
        CommentNotFoundException exception = assertThrows(
                CommentNotFoundException.class,
                () -> commentService.delete(userId, commentId)
        );

        assertTrue(exception.getMessage().contains("Comment not found"));
        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).deleteComment(any());
    }

    @Test
    void delete_UserNotOwner_ThrowsUnauthorizedException() {
        // Arrange
        Long wrongUserId = 999L; // Different user, not the owner
        Long commentId = 1L;

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> commentService.delete(wrongUserId, commentId)
        );

        assertTrue(exception.getMessage().contains("User is not authorized to delete this comment"));
        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).deleteComment(any());
    }

    // ADDITIONAL EDGE CASE TESTS
    @Test
    void create_VerifiesCommentEntitySetup() {
        // Arrange
        Long userId = 1L;
        Long reservationId = 1L;
        Long accommodationId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(testReservation));
        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(testAccommodation));
        when(commentRepository.existsByReservation_Id(reservationId)).thenReturn(false);
        when(commentMapper.toEntity(createCommentDTO)).thenReturn(testComment);
        when(commentRepository.save(any(CommentEntity.class))).thenReturn(testComment);
        when(commentMapper.toCommentDTO(testComment)).thenReturn(testCommentDTO);

        // Act
        commentService.create(userId, reservationId, createCommentDTO, accommodationId);

        // Assert
        verify(commentRepository).save(argThat(comment ->
                comment.getIsModerated().equals(false) &&
                comment.getReservation().equals(testReservation) &&
                comment.getAccommodation().equals(testAccommodation) &&
                comment.getUser().equals(testUser) &&
                comment.getHostReply() != null &&
                comment.getHostReply().getHostId().equals(testHost.getId())
        ));
    }

    @Test
    void findByAccommodation_MultipleComments_ReturnsAll() {
        // Arrange
        Long accommodationId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        CommentEntity comment2 = new CommentEntity();
        comment2.setId(2L);
        comment2.setRating(4);
        comment2.setText("Good place");

        Page<CommentEntity> commentPage = new PageImpl<>(List.of(testComment, comment2));

        when(accommodationRepository.existsById(accommodationId)).thenReturn(true);
        when(commentRepository.findAllByAccommodation_Id(accommodationId, pageable)).thenReturn(commentPage);
        when(commentMapper.toCommentDTO(any(CommentEntity.class))).thenReturn(testCommentDTO);

        // Act
        Page<CommentDTO> result = commentService.findByAccommodation(accommodationId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(commentMapper, times(2)).toCommentDTO(any(CommentEntity.class));
    }

    @Test
    void findByUser_MultipleComments_ReturnsAll() {
        // Arrange
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        CommentEntity comment2 = new CommentEntity();
        comment2.setId(2L);
        comment2.setRating(3);
        comment2.setText("Average");

        Page<CommentEntity> commentPage = new PageImpl<>(List.of(testComment, comment2));

        when(userRepository.existsById(userId)).thenReturn(true);
        when(commentRepository.findAllByUser_Id(userId, pageable)).thenReturn(commentPage);
        when(commentMapper.toCommentDTO(any(CommentEntity.class))).thenReturn(testCommentDTO);

        // Act
        Page<CommentDTO> result = commentService.findByUser(userId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(commentMapper, times(2)).toCommentDTO(any(CommentEntity.class));
    }
}
