package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.services.UserDocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserDocumentServiceImpl implements UserDocumentService {
    @Override
    public void upload(Long userId, List<String> urlFiles) {

    }

    @Override
    public void delete(Long documentId) {

    }

    @Override
    public List<String> listByUser(Long userId) {
        return List.of();
    }
}
