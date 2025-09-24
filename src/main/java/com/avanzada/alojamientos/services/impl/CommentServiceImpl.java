package com.avanzada.alojamientos.services.impl;


import com.avanzada.alojamientos.DTO.CommentDTO;
import com.avanzada.alojamientos.DTO.CreateCommentDTO;
import com.avanzada.alojamientos.services.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class CommentServiceImpl implements CommentService {
    @Override
    public CommentDTO create(Long userId, Long reservationId, CreateCommentDTO dto, Long accommodationId) {
        return null;
    }

    @Override
    public Optional<CommentDTO> findById(Long commentId) {
        return Optional.empty();
    }

    @Override
    public Page<CommentDTO> findByAccommodation(Long accommodationId, Pageable pageable) {
        return null;
    }

    @Override
    public Page<CommentDTO> findByUser(Long userId, Pageable pageable) {
        return null;
    }

    @Override
    public void reply(Long commentId, Long hostId, String replyText) {
        log.info("Replying to comment ID: {} by host ID: {} with text: {}", commentId, hostId, replyText);
    }

    @Override
    public void moderate(Long commentId, boolean approved) {
        log.info("Moderating comment ID: {}. Approved: {}", commentId, approved);
    }

    @Override
    public void delete(Long commentId) {
        log.info("Deleting comment with ID: {}", commentId);
    }
}
