package com.avanzada.alojamientos.DTO;

public record   CommentDTO (
        String id,
        String reservationId,
        String accommodationId,
        String userId,
        Integer rating,
        String text,
        String createdAt,
        String replyAt,
        Boolean isModerated
){

}
