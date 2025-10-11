package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.other.FavoriteDTO;
import com.avanzada.alojamientos.entities.AccommodationEntity;
import com.avanzada.alojamientos.entities.FavoriteEntity;
import com.avanzada.alojamientos.entities.UserEntity;
import com.avanzada.alojamientos.exceptions.AccommodationNotFoundException;
import com.avanzada.alojamientos.exceptions.FavoriteAlreadyExistsException;
import com.avanzada.alojamientos.exceptions.FavoriteNotFoundException;
import com.avanzada.alojamientos.exceptions.UserNotFoundException;
import com.avanzada.alojamientos.mappers.FavoriteMapper;
import com.avanzada.alojamientos.repositories.AccommodationRepository;
import com.avanzada.alojamientos.repositories.FavoriteRepository;
import com.avanzada.alojamientos.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceImplTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private FavoriteMapper favoriteMapper;

    @InjectMocks
    private FavoriteServiceImpl favoriteService;

    private UserEntity testUser;
    private AccommodationEntity testAccommodation;
    private FavoriteEntity testFavorite;
    private FavoriteDTO testFavoriteDTO;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new UserEntity();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        // Setup test accommodation
        testAccommodation = new AccommodationEntity();
        testAccommodation.setId(1L);
        testAccommodation.setTitle("Test Accommodation");
        testAccommodation.setDescription("Test Description");

        // Setup test favorite entity
        testFavorite = new FavoriteEntity();
        testFavorite.setId(1L);
        testFavorite.setUser(testUser);
        testFavorite.setAccommodation(testAccommodation);
        testFavorite.setCreatedAt(LocalDateTime.now());

        // Setup test favorite DTO
        testFavoriteDTO = new FavoriteDTO(
                1L,
                1L,
                null,
                LocalDateTime.now().toString()
        );
    }

    // ADD TESTS
    @Test
    void add_Success() {
        // Arrange
        Long userId = 1L;
        Long accommodationId = 1L;

        when(favoriteRepository.existsByUserIdAndAccommodationId(userId, accommodationId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(testAccommodation));
        when(favoriteRepository.save(any(FavoriteEntity.class))).thenReturn(testFavorite);
        when(favoriteMapper.toDTO(testFavorite)).thenReturn(testFavoriteDTO);

        // Act
        FavoriteDTO result = favoriteService.add(userId, accommodationId);

        // Assert
        assertNotNull(result);
        assertEquals(testFavoriteDTO, result);
        verify(favoriteRepository).existsByUserIdAndAccommodationId(userId, accommodationId);
        verify(userRepository).findById(userId);
        verify(accommodationRepository).findById(accommodationId);
        verify(favoriteRepository).save(any(FavoriteEntity.class));
        verify(favoriteMapper).toDTO(testFavorite);
    }

    @Test
    void add_FavoriteAlreadyExists_ThrowsFavoriteAlreadyExistsException() {
        // Arrange
        Long userId = 1L;
        Long accommodationId = 1L;

        when(favoriteRepository.existsByUserIdAndAccommodationId(userId, accommodationId)).thenReturn(true);

        // Act & Assert
        FavoriteAlreadyExistsException exception = assertThrows(
                FavoriteAlreadyExistsException.class,
                () -> favoriteService.add(userId, accommodationId)
        );

        assertTrue(exception.getMessage().contains("El favorito ya existe"));
        verify(favoriteRepository).existsByUserIdAndAccommodationId(userId, accommodationId);
        verify(userRepository, never()).findById(any());
        verify(accommodationRepository, never()).findById(any());
        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void add_UserNotFound_ThrowsUserNotFoundException() {
        // Arrange
        Long userId = 1L;
        Long accommodationId = 1L;

        when(favoriteRepository.existsByUserIdAndAccommodationId(userId, accommodationId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> favoriteService.add(userId, accommodationId)
        );

        assertTrue(exception.getMessage().contains("Usuario no encontrado"));
        assertTrue(exception.getMessage().contains(userId.toString()));
        verify(favoriteRepository).existsByUserIdAndAccommodationId(userId, accommodationId);
        verify(userRepository).findById(userId);
        verify(accommodationRepository, never()).findById(any());
        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void add_AccommodationNotFound_ThrowsAccommodationNotFoundException() {
        // Arrange
        Long userId = 1L;
        Long accommodationId = 1L;

        when(favoriteRepository.existsByUserIdAndAccommodationId(userId, accommodationId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.empty());

        // Act & Assert
        AccommodationNotFoundException exception = assertThrows(
                AccommodationNotFoundException.class,
                () -> favoriteService.add(userId, accommodationId)
        );

        assertTrue(exception.getMessage().contains("Alojamiento no encontrado"));
        assertTrue(exception.getMessage().contains(accommodationId.toString()));
        verify(favoriteRepository).existsByUserIdAndAccommodationId(userId, accommodationId);
        verify(userRepository).findById(userId);
        verify(accommodationRepository).findById(accommodationId);
        verify(favoriteRepository, never()).save(any());
    }

    // REMOVE TESTS
    @Test
    void remove_Success() {
        // Arrange
        Long favoriteId = 1L;

        when(favoriteRepository.findById(favoriteId)).thenReturn(Optional.of(testFavorite));

        // Act
        favoriteService.remove(favoriteId);

        // Assert
        verify(favoriteRepository).findById(favoriteId);
        verify(favoriteRepository).delete(testFavorite);
    }

    @Test
    void remove_FavoriteNotFound_ThrowsFavoriteNotFoundException() {
        // Arrange
        Long favoriteId = 1L;

        when(favoriteRepository.findById(favoriteId)).thenReturn(Optional.empty());

        // Act & Assert
        FavoriteNotFoundException exception = assertThrows(
                FavoriteNotFoundException.class,
                () -> favoriteService.remove(favoriteId)
        );

        assertTrue(exception.getMessage().contains("Favorito no encontrado"));
        assertTrue(exception.getMessage().contains(favoriteId.toString()));
        verify(favoriteRepository).findById(favoriteId);
        verify(favoriteRepository, never()).delete(any());
    }

    // FIND BY USER TESTS
    @Test
    void findByUser_Success() {
        // Arrange
        Long userId = 1L;
        List<FavoriteEntity> favorites = List.of(testFavorite);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(favoriteRepository.findByUserId(userId)).thenReturn(favorites);
        when(favoriteMapper.toDTO(testFavorite)).thenReturn(testFavoriteDTO);

        // Act
        List<FavoriteDTO> result = favoriteService.findByUser(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testFavoriteDTO, result.getFirst());
        verify(userRepository).existsById(userId);
        verify(favoriteRepository).findByUserId(userId);
        verify(favoriteMapper).toDTO(testFavorite);
    }

    @Test
    void findByUser_UserNotFound_ThrowsUserNotFoundException() {
        // Arrange
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(false);

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> favoriteService.findByUser(userId)
        );

        assertTrue(exception.getMessage().contains("Usuario no encontrado"));
        assertTrue(exception.getMessage().contains(userId.toString()));
        verify(userRepository).existsById(userId);
        verify(favoriteRepository, never()).findByUserId(any());
    }

    @Test
    void findByUser_NoFavorites_ReturnsEmptyList() {
        // Arrange
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);
        when(favoriteRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        // Act
        List<FavoriteDTO> result = favoriteService.findByUser(userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).existsById(userId);
        verify(favoriteRepository).findByUserId(userId);
        verify(favoriteMapper, never()).toDTO(any());
    }

    @Test
    void findByUser_MultipleFavorites_ReturnsAllFavorites() {
        // Arrange
        Long userId = 1L;

        FavoriteEntity favorite2 = new FavoriteEntity();
        favorite2.setId(2L);
        favorite2.setUser(testUser);
        AccommodationEntity accommodation2 = new AccommodationEntity();
        accommodation2.setId(2L);
        favorite2.setAccommodation(accommodation2);
        favorite2.setCreatedAt(LocalDateTime.now());

        FavoriteDTO favoriteDTO2 = new FavoriteDTO(
                2L,
                1L,
                null,
                LocalDateTime.now().toString()
        );

        List<FavoriteEntity> favorites = List.of(testFavorite, favorite2);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(favoriteRepository.findByUserId(userId)).thenReturn(favorites);
        when(favoriteMapper.toDTO(testFavorite)).thenReturn(testFavoriteDTO);
        when(favoriteMapper.toDTO(favorite2)).thenReturn(favoriteDTO2);

        // Act
        List<FavoriteDTO> result = favoriteService.findByUser(userId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testFavoriteDTO, result.get(0));
        assertEquals(favoriteDTO2, result.get(1));
        verify(userRepository).existsById(userId);
        verify(favoriteRepository).findByUserId(userId);
        verify(favoriteMapper, times(2)).toDTO(any(FavoriteEntity.class));
    }

    // ADDITIONAL EDGE CASE TESTS
    @Test
    void add_VerifiesFavoriteEntityCreation() {
        // Arrange
        Long userId = 1L;
        Long accommodationId = 1L;

        when(favoriteRepository.existsByUserIdAndAccommodationId(userId, accommodationId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(testAccommodation));
        when(favoriteRepository.save(any(FavoriteEntity.class))).thenReturn(testFavorite);
        when(favoriteMapper.toDTO(testFavorite)).thenReturn(testFavoriteDTO);

        // Act
        favoriteService.add(userId, accommodationId);

        // Assert
        verify(favoriteRepository).save(argThat(favorite ->
                favorite.getUser().equals(testUser) &&
                favorite.getAccommodation().equals(testAccommodation)
        ));
    }

    @Test
    void remove_WithDifferentFavoriteId_Success() {
        // Arrange
        Long favoriteId = 999L;
        FavoriteEntity differentFavorite = new FavoriteEntity();
        differentFavorite.setId(favoriteId);
        differentFavorite.setUser(testUser);
        differentFavorite.setAccommodation(testAccommodation);

        when(favoriteRepository.findById(favoriteId)).thenReturn(Optional.of(differentFavorite));

        // Act
        favoriteService.remove(favoriteId);

        // Assert
        verify(favoriteRepository).findById(favoriteId);
        verify(favoriteRepository).delete(differentFavorite);
    }

    @Test
    void findByUser_WithDifferentUserId_Success() {
        // Arrange
        Long differentUserId = 999L;
        UserEntity differentUser = new UserEntity();
        differentUser.setId(differentUserId);

        FavoriteEntity favoriteForDifferentUser = new FavoriteEntity();
        favoriteForDifferentUser.setId(1L);
        favoriteForDifferentUser.setUser(differentUser);
        favoriteForDifferentUser.setAccommodation(testAccommodation);

        FavoriteDTO differentUserFavoriteDTO = new FavoriteDTO(
                1L,
                differentUserId,
                null,
                LocalDateTime.now().toString()
        );

        when(userRepository.existsById(differentUserId)).thenReturn(true);
        when(favoriteRepository.findByUserId(differentUserId)).thenReturn(List.of(favoriteForDifferentUser));
        when(favoriteMapper.toDTO(favoriteForDifferentUser)).thenReturn(differentUserFavoriteDTO);

        // Act
        List<FavoriteDTO> result = favoriteService.findByUser(differentUserId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(differentUserId, result.getFirst().userId());
        verify(userRepository).existsById(differentUserId);
        verify(favoriteRepository).findByUserId(differentUserId);
    }
}