package com.avanzada.alojamientos.services;


import java.util.List;

public interface UserDocumentService {
    void upload(Long userId, List<String> urlFiles);
    void delete(Long documentId);
    List<String> listByUser(Long userId);
}
