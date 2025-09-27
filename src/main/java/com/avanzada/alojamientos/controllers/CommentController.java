package com.avanzada.alojamientos.controllers;


import com.avanzada.alojamientos.DTO.CommentDTO;
import com.avanzada.alojamientos.DTO.CreateCommentDTO;
import com.avanzada.alojamientos.services.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{userId}/{reservationId}/{accommodationId}")
    public CommentDTO create(@PathVariable Long userId,
                             @PathVariable Long reservationId,
                             @PathVariable Long accommodationId,
                             @RequestBody @Valid CreateCommentDTO dto) {
        return commentService.create(userId, reservationId, dto, accommodationId);
    }

    @GetMapping("/{commentId}")
    public Optional<CommentDTO> findById(@PathVariable Long commentId) {
        return commentService.findById(commentId);
    }

    @GetMapping("/accommodation/{accommodationId}")
    public Page<CommentDTO> findByAccommodation(@PathVariable Long accommodationId, Pageable pageable) {
        return commentService.findByAccommodation(accommodationId, pageable);
    }

    @GetMapping("/user/{userId}")
    public Page<CommentDTO> findByUser(@PathVariable Long userId, Pageable pageable) {
        return commentService.findByUser(userId, pageable);
    }

    @PutMapping("/{commentId}/reply")
    public void reply(@PathVariable Long commentId,
                      @RequestParam Long hostId,
                      @RequestParam String replyText) {
        commentService.reply(commentId, hostId, replyText);
    }

    @PutMapping("/{commentId}/moderate")
    public void moderate(@PathVariable Long commentId,
                         @RequestParam boolean approved) {
        commentService.moderate(commentId, approved);
    }

    @DeleteMapping("/{commentId}")
    public void delete(@PathVariable Long commentId) {
        commentService.delete(commentId);
    }
}
