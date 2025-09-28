package com.avanzada.alojamientos.DTO.comment;

import com.avanzada.alojamientos.DTO.host.ReplyHostDTO;

import java.time.LocalDateTime;

public record   CommentDTO (
        long id,
        Integer rating,
        String text,
        LocalDateTime createdAt,
        Boolean isModerated,
        Integer reservationId,
        Integer accommodationId,
        long userId,
        ReplyHostDTO hostReply
){

}
