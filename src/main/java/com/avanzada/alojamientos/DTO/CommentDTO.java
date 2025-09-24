package com.avanzada.alojamientos.DTO;

import java.time.LocalDateTime;

public record   CommentDTO (
        Integer id,
        Integer rating,
        String text,
        LocalDateTime createdAt,
        Boolean isModerated,
        Integer reservationId,
        Integer accommodationId,
        Integer userId,
        ReplyHostDTO hostReply
){

}
