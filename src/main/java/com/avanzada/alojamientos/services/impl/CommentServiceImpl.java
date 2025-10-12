package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.comment.CommentDTO;
import com.avanzada.alojamientos.DTO.comment.CreateCommentDTO;
import com.avanzada.alojamientos.DTO.model.HostReply;
import com.avanzada.alojamientos.entities.AccommodationEntity;
import com.avanzada.alojamientos.entities.CommentEntity;
import com.avanzada.alojamientos.entities.ReservationEntity;
import com.avanzada.alojamientos.entities.UserEntity;
import com.avanzada.alojamientos.exceptions.*;
import com.avanzada.alojamientos.mappers.CommentMapper;
import com.avanzada.alojamientos.repositories.AccommodationRepository;
import com.avanzada.alojamientos.repositories.CommentRepository;
import com.avanzada.alojamientos.repositories.ReservationRepository;
import com.avanzada.alojamientos.repositories.UserRepository;
import com.avanzada.alojamientos.services.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;


@Service
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    public static final String COMMENT_NOT_FOUND_MESSAGE = "Comment not found with ID: ";
    private final CommentRepository commentRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final AccommodationRepository accommodationRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDTO create(Long userId, Long reservationId, CreateCommentDTO dto, Long accommodationId) {
        log.info("Creating comment for reservation ID: {} by user ID: {}", reservationId, userId);

        // Find user
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        // Find reservation
        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new UserNotFoundException("Reservation not found with ID: " + reservationId));

        // Find accommodation
        AccommodationEntity accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new UserNotFoundException("Accommodation not found with ID: " + accommodationId));

        // Validate reservation belongs to user
        if (!reservation.getUser().getId().equals(userId)) {
            throw new CommentForbiddenException("User cannot comment on a reservation they did not make");
        }

        // Validate reservation is for the specified accommodation
        if (!reservation.getAccommodation().getId().equals(accommodationId)) {
            throw new CommentForbiddenException("Reservation is not for the specified accommodation");
        }

        // Validate reservation is completed
        if (LocalDate.now().isBefore(reservation.getEndDate())) {
            throw new CommentForbiddenException("Cannot comment on a reservation that hasn't been completed");
        }

        // Check if user already commented on this reservation
        if (commentRepository.existsByReservation_Id(reservationId)) {
            throw new CommentForbiddenException("User has already commented on this reservation");
        }

        // Create and save the comment
        CommentEntity comment = commentMapper.toEntity(dto);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setIsModerated(Boolean.FALSE);
        comment.setReservation(reservation);
        comment.setAccommodation(accommodation);
        comment.setUser(user);

        // Initialize HostReply with the accommodation's host ID
        HostReply hostReply = new HostReply();
        hostReply.setHostId(accommodation.getHost().getId());
        hostReply.setReply(null); // No reply initially
        hostReply.setReplyAt(null); // No reply date initially
        comment.setHostReply(hostReply);

        CommentEntity savedComment = commentRepository.save(comment);

        return commentMapper.toCommentDTO(savedComment);
    }

    @Override
    public CommentDTO findById(Long commentId) {
        log.info("Finding comment by ID: {}", commentId);
        CommentEntity comment =  commentRepository.findById(commentId).orElseThrow(() -> new CommentNotFoundException(COMMENT_NOT_FOUND_MESSAGE + commentId));
        return commentMapper.toCommentDTO(comment);
    }

    @Override
    public Page<CommentDTO> findByAccommodation(Long accommodationId, Pageable pageable) {
        log.info("Finding comments for accommodation ID: {}", accommodationId);

        // Verify accommodation exists
        if (!accommodationRepository.existsById(accommodationId)) {
            throw new AccommodationNotFoundException("Accommodation not found with ID: " + accommodationId);
        }

        return commentRepository.findAllByAccommodation_Id(accommodationId, pageable)
                .map(commentMapper::toCommentDTO);
    }

    @Override
    public Page<CommentDTO> findByUser(Long userId, Pageable pageable) {
        log.info("Finding comments by user ID: {}", userId);

        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }

        return commentRepository.findAllByUser_Id(userId, pageable)
                .map(commentMapper::toCommentDTO);
    }

    @Override
    @Transactional
    public void reply(Long commentId, Long hostId, String replyText) {
        log.info("Replying to comment ID: {} by host ID: {} with text: {}", commentId, hostId, replyText);

        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(COMMENT_NOT_FOUND_MESSAGE + commentId));

        // Verify host owns the accommodation
        AccommodationEntity accommodation = comment.getAccommodation();
        if (!accommodation.getHost().getId().equals(hostId)) {
            throw new CommentForbiddenException("Only the host of the accommodation can reply to this comment");
        }

        // Create host reply
        HostReply hostReply = new HostReply();
        hostReply.setHostId(hostId);
        hostReply.setReply(replyText);
        hostReply.setReplyAt(LocalDateTime.now());

        // Update comment with host reply
        comment.setHostReply(hostReply);
        commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void moderate(Long commentId, boolean approved) {
        log.info("Moderating comment ID: {}. Approved: {}", commentId, approved);

        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(COMMENT_NOT_FOUND_MESSAGE + commentId));

        comment.setIsModerated(true);

        // If comment is not approved, it could be soft deleted or flagged
        if (!approved) {
            commentRepository.deleteById(commentId);
        } else {
            commentRepository.save(comment);
        }
    }

    @Override
    @Transactional
    public void delete(Long userId, Long commentId) {
        log.info("Deleting comment with ID: {}", commentId);

        CommentEntity comment= commentRepository.findById(commentId).orElseThrow(() -> new CommentNotFoundException(COMMENT_NOT_FOUND_MESSAGE + commentId));
        if (!comment.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("User cannot delete a comment he hasn't written");
        }
        commentRepository.deleteComment(commentId);
        log.info("Comment with ID: {} deleted successfully", commentId);
        }
}