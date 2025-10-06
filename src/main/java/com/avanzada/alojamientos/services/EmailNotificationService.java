package com.avanzada.alojamientos.services;

import com.avanzada.alojamientos.DTO.notification.EmailDTO;

public interface EmailNotificationService extends NotificationService{
    void sendMail(EmailDTO emailDTO) throws Exception;
}
