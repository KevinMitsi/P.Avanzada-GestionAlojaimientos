package com.avanzada.alojamientos.controllers;


import com.avanzada.alojamientos.DTO.comment.CommentDTO;
import com.avanzada.alojamientos.DTO.comment.CreateCommentDTO;
import com.avanzada.alojamientos.security.CurrentUserService;
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
    private final CurrentUserService currentUserService;

    @PostMapping("/{reservationId}/{accommodationId}")
    public ResponseEntity<CommentDTO> create(@PathVariable Long reservationId,
                             @PathVariable Long accommodationId,
                             @RequestBody @Valid CreateCommentDTO dto) {
        Long userId = currentUserService.getCurrentUserId();
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

    @GetMapping("/user/me")
    public Page<CommentDTO> findByUser(Pageable pageable) {
        Long userId = currentUserService.getCurrentUserId();
        return commentService.findByUser(userId, pageable);
    }

    @PutMapping("/{commentId}/reply")
    public ResponseEntity<String> reply(@PathVariable Long commentId,
                      @RequestParam String replyText) {
        Long hostId = currentUserService.getCurrentHostId();
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
        commentService.delete(currentUserService.getCurrentUserId(),commentId);
        return ResponseEntity.noContent().build();
    }
}
