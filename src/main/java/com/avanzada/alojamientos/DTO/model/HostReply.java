package com.avanzada.alojamientos.DTO.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HostReply {

    @Column(name = "host_id")
    private Long hostId;

    @Column(columnDefinition = "TEXT", name = "reply")
    private String reply;

    @Column(name = "reply_at")
    private LocalDateTime replyAt;
}
