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
import com.avanzada.alojamientos.services.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final AccommodationRepository accommodationRepository;
    private final FavoriteMapper favoriteMapper;

    @Override
    public FavoriteDTO add(Long userId, Long accommodationId) {
        log.info("Adding favorite for user {} and accommodation {}", userId, accommodationId);

        // Verificar si el favorito ya existe
        if (favoriteRepository.existsByUserIdAndAccommodationId(userId, accommodationId)) {
            throw new FavoriteAlreadyExistsException(
                "El favorito ya existe para el usuario " + userId + " y alojamiento " + accommodationId
            );
        }

        // Buscar el usuario
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));

        // Buscar el alojamiento
        AccommodationEntity accommodation = accommodationRepository.findById(accommodationId)
            .orElseThrow(() -> new AccommodationNotFoundException("Alojamiento no encontrado con ID: " + accommodationId));

        // Crear y guardar el favorito
        FavoriteEntity favoriteEntity = new FavoriteEntity();
        favoriteEntity.setUser(user);
        favoriteEntity.setAccommodation(accommodation);

        FavoriteEntity savedFavorite = favoriteRepository.save(favoriteEntity);

        log.info("Favorite created successfully with ID: {}", savedFavorite.getId());
        return favoriteMapper.toDTO(savedFavorite);
    }

    @Override
    public void remove(Long favoriteId) {
        log.info("Removing favorite with ID: {}", favoriteId);

        FavoriteEntity favorite = favoriteRepository.findById(favoriteId)
            .orElseThrow(() -> new FavoriteNotFoundException("Favorito no encontrado con ID: " + favoriteId));

        favoriteRepository.delete(favorite);
        log.info("Favorite with ID {} removed successfully", favoriteId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteDTO> findByUser(Long userId) {
        log.info("Finding favorites for user: {}", userId);

        // Verificar que el usuario existe
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Usuario no encontrado con ID: " + userId);
        }

        List<FavoriteEntity> favorites = favoriteRepository.findByUserId(userId);

        log.info("Found {} favorites for user {}", favorites.size(), userId);
        return favorites.stream()
            .map(favoriteMapper::toDTO)
            .toList();
    }
}
