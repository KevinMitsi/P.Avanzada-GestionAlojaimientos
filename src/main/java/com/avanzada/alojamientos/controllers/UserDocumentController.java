package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.services.UserDocumentService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-documents")
@RequiredArgsConstructor
public class UserDocumentController {

    private final UserDocumentService userDocumentService;

    @PostMapping("/{userId}")
    public void upload(@PathVariable Long userId, @RequestBody List<String> fileUrls) {
        userDocumentService.upload(userId, fileUrls);
    }

    @DeleteMapping("/{documentId}")
    public void delete(@PathVariable Long documentId) {
        userDocumentService.delete(documentId);
    }

    @GetMapping("/{userId}")
    public List<String> listByUser(@PathVariable Long userId) {
        return userDocumentService.listByUser(userId);
    }
}
