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

    @Column(nullable = false)
    private String hostId;

    @Column(columnDefinition = "TEXT", length = 500, nullable = false)
    private String reply;

    private LocalDateTime replyAt;
}
