package com.avanzada.alojamientos.DTO.comment;

import com.avanzada.alojamientos.DTO.host.ReplyHostDTO;

import java.time.LocalDateTime;

public record   CommentDTO (
        Long id,
        Integer rating,
        String text,
        LocalDateTime createdAt,
        Boolean isModerated,
        Integer reservationId,
        Integer accommodationId,
        Long userId,
        ReplyHostDTO hostReply
){

}
