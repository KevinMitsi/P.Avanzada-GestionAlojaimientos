package com.avanzada.alojamientos.services;


import com.avanzada.alojamientos.DTO.comment.CommentDTO;
import com.avanzada.alojamientos.DTO.comment.CreateCommentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;



public interface CommentService {
    CommentDTO create(Long userId, Long reservationId, CreateCommentDTO dto, Long accommodationId);
    CommentDTO findById(Long commentId);

    Page<CommentDTO> findByAccommodation(Long accommodationId, Pageable pageable);
    Page<CommentDTO> findByUser(Long userId, Pageable pageable);

    // Host responde comentario
    void reply(Long commentId, Long hostId, String replyText);

    // Moderación (admin)
    void moderate(Long commentId, boolean approved);

    void delete(Long userId, Long commentId);
}

