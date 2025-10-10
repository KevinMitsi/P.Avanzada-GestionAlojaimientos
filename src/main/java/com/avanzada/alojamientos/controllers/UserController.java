package com.avanzada.alojamientos.controllers;


import com.avanzada.alojamientos.DTO.auth.PasswordChangeDTO;
import com.avanzada.alojamientos.DTO.user.UserDTO;
import com.avanzada.alojamientos.DTO.user.EditUserDTO;
import com.avanzada.alojamientos.exceptions.DeletingStorageException;
import com.avanzada.alojamientos.exceptions.UploadingStorageException;
import com.avanzada.alojamientos.security.CurrentUserService;
import com.avanzada.alojamientos.services.UserDocumentService;
import com.avanzada.alojamientos.services.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserDocumentService userDocumentService;
    private final CurrentUserService currentUserService;

    @GetMapping("/admin/{userId}")
    public Optional<UserDTO> findById(@PathVariable Long userId) {
        return userService.findById(userId);
    }

    @PutMapping("/{userId}/enable")
   public void enable(@PathVariable String userId, @RequestParam boolean enable) {
        userService.enable(userId, enable);
    }

    @PutMapping
    public ResponseEntity<UserDTO> editProfile(@Valid @RequestBody EditUserDTO editUserDTO) {
        Long userId = currentUserService.getCurrentUserId();
        UserDTO updatedUser = userService.editProfile(userId, editUserDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteProfile() {
        Long userId = currentUserService.getCurrentUserId();
        userService.deleteProfile(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/password")
    public ResponseEntity<String> changePassword(@RequestBody @Valid PasswordChangeDTO passwordChangeDTO) {
        userService.changePassword(currentUserService.getCurrentUserId()
                , passwordChangeDTO.currentPassword()
                , passwordChangeDTO.newPassword());
        return ResponseEntity.accepted().body("Contraseña cambiada exitosamente");
    }

    @PostMapping("/documents")
    public ResponseEntity<List<String>> uploadDocuments(
            @RequestParam("documents") List<MultipartFile> documentFiles) throws UploadingStorageException {
        Long userId = currentUserService.getCurrentUserId();
        List<String> uploadedUrls = userDocumentService.uploadDocuments(userId, documentFiles);
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedUrls);
    }

    @DeleteMapping("/documents/{documentIndex}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long documentIndex) throws DeletingStorageException {
        Long userId = currentUserService.getCurrentUserId();
        userDocumentService.deleteDocument(userId, documentIndex);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/profile-image")
    public ResponseEntity<String> uploadProfileImage(
            @RequestParam("image") MultipartFile imageFile) throws UploadingStorageException {
        Long userId = currentUserService.getCurrentUserId();
        String imageUrl = userService.uploadProfileImage(userId, imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(imageUrl);
    }

    @DeleteMapping("/profile-image")
    public ResponseEntity<Void> deleteProfileImage() throws DeletingStorageException {
        Long userId = currentUserService.getCurrentUserId();
        userService.deleteProfileImage(userId);
        return ResponseEntity.noContent().build();
    }

}
