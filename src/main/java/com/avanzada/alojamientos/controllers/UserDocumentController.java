package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.security.CurrentUserService;
import com.avanzada.alojamientos.services.UserDocumentService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-documents")
@RequiredArgsConstructor
public class UserDocumentController {

    private final UserDocumentService userDocumentService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public void upload(@RequestBody List<String> fileUrls) {
        Long userId = currentUserService.getCurrentUserId();
        userDocumentService.upload(userId, fileUrls);
    }

    @DeleteMapping("/{documentId}")
    public void delete(@PathVariable Long documentId) {
        userDocumentService.delete(documentId);
    }

    @GetMapping("/me")
    public List<String> listByUser() {
        Long userId = currentUserService.getCurrentUserId();
        return userDocumentService.listByUser(userId);
    }
}
