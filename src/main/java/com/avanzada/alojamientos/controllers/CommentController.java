package com.avanzada.alojamientos.controllers;


import com.avanzada.alojamientos.DTO.comment.CommentDTO;
import com.avanzada.alojamientos.DTO.comment.CreateCommentDTO;
import com.avanzada.alojamientos.services.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{userId}/{reservationId}/{accommodationId}")
    public ResponseEntity<CommentDTO> create(@PathVariable Long userId,
                             @PathVariable Long reservationId,
                             @PathVariable Long accommodationId,
                             @RequestBody @Valid CreateCommentDTO dto) {
        return ResponseEntity.ok(commentService.create(userId, reservationId, dto, accommodationId));
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDTO> findById(@PathVariable Long commentId) {
        return ResponseEntity.ok(commentService.findById(commentId));
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
    public ResponseEntity<String> reply(@PathVariable Long commentId,
                      @RequestParam Long hostId,
                      @RequestParam String replyText) {
        commentService.reply(commentId, hostId, replyText);
        return ResponseEntity.ok("Reply added successfully");
    }

    @PutMapping("/{commentId}/moderate")
    public ResponseEntity<String> moderate(@PathVariable Long commentId,
                         @RequestParam Boolean approved) {
        commentService.moderate(commentId, approved);
        return ResponseEntity.ok("Comment moderated successfully");
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable Long commentId) {
        commentService.delete(commentId);
        return ResponseEntity.noContent().build();
    }
}
