package com.avanzada.alojamientos.DTO.comment;

import com.avanzada.alojamientos.DTO.host.ReplyHostDTO;



import java.time.LocalDateTime;

public record   CommentDTO (
        Long id,
        Float rating,
        String text,
        LocalDateTime createdAt,
        Boolean isModerated,
        Long reservationId,
        Long accommodationId,
        Long userId,
        ReplyHostDTO hostReply
){

}
