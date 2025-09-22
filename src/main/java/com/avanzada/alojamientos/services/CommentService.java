package com.avanzada.alojamientos.services;

import com.avanzada.alojamientos.DTO.CommentCreateDTO;
import com.avanzada.alojamientos.DTO.CommentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CommentService {
    CommentDTO create(Long userId, Long reservationId, CommentCreateDTO dto, Long accommodationId);
    Optional<CommentDTO> findById(Long commentId);

    Page<CommentDTO> findByAccommodation(Long accommodationId, Pageable pageable);
    Page<CommentDTO> findByUser(Long userId, Pageable pageable);

    // Host responde comentario
    void reply(Long commentId, Long hostId, String replyText);

    // Moderación (admin)
    void moderate(Long commentId, boolean approved);

    void delete(Long commentId);
}

