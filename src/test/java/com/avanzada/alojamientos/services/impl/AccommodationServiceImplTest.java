package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.accommodation.*;
import com.avanzada.alojamientos.DTO.other.CoordinatesDTO;
import com.avanzada.alojamientos.entities.*;
import com.avanzada.alojamientos.exceptions.*;
import com.avanzada.alojamientos.mappers.AccommodationMapper;
import com.avanzada.alojamientos.repositories.AccommodationRepository;
import com.avanzada.alojamientos.repositories.ImageRepository;
import com.avanzada.alojamientos.services.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccommodationServiceImplTest {

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private AccommodationMapper accommodationMapper;

    @Mock
    private StorageService storageService;

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private AccommodationServiceImpl accommodationService;

    private AccommodationEntity testAccommodationEntity;
    private CreateAccommodationDTO createAccommodationDTO;
    private AccommodationDTO accommodationDTO;
    private UpdateAccommodationDTO updateAccommodationDTO;

    @BeforeEach
    void setUp() {
        // Setup test entities and DTOs
        testAccommodationEntity = new AccommodationEntity();
        testAccommodationEntity.setId(1L);
        testAccommodationEntity.setTitle("Test Accommodation");
        testAccommodationEntity.setDescription("Test Description");
        testAccommodationEntity.setPricePerNight(new BigDecimal("100.00"));
        testAccommodationEntity.setMaxGuests(4);
        testAccommodationEntity.setActive(true);
        testAccommodationEntity.setSoftDeleted(false);
        testAccommodationEntity.setCreatedAt(LocalDateTime.now());

        UserEntity host = new UserEntity();
        host.setId(1L);
        testAccommodationEntity.setHost(host);

        createAccommodationDTO = new CreateAccommodationDTO(
                "Test Accommodation",
                "Test Description",
                1L,
                new CoordinatesDTO(40.7128, -74.0060),
                "123 Test Street",
                new BigDecimal("100.00"),
                List.of("WiFi", "Pool"),
                4
        );

        accommodationDTO = new AccommodationDTO(
                1L, 1L, "Test Accommodation", "Test Description",
                null, "123 Test Street", null, new BigDecimal("100.00"),
                List.of("WiFi", "Pool"), new ArrayList<>(), 4, true, false,
                null, null, null, 0, 0.0
        );

        updateAccommodationDTO = new UpdateAccommodationDTO(
                "Updated Title",
                "Updated Description",
                "Updated Address",
                new CoordinatesDTO(40.7580, -73.9855),
                new BigDecimal("150.00"),
                List.of("WiFi", "Pool", "Gym"),
                6,
                true
        );
    }

    // CREATE TESTS
    @Test
    void create_Success() {
        // Arrange
        CreateAccommodationResponseDTO expectedResponse = new CreateAccommodationResponseDTO(
                1L, 1L, "Test Accommodation", "Test Description", null,
                "123 Test Street", null, new BigDecimal("100.00"),
                List.of("WiFi", "Pool"), 4, "2024-01-01T10:00:00"
        );
        when(accommodationMapper.toEntity(createAccommodationDTO)).thenReturn(testAccommodationEntity);
        when(accommodationRepository.save(any(AccommodationEntity.class))).thenReturn(testAccommodationEntity);
        when(accommodationMapper.toCreateAccommodationResponseDTO(testAccommodationEntity)).thenReturn(expectedResponse);

        // Act
        CreateAccommodationResponseDTO result = accommodationService.create(createAccommodationDTO, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Test Accommodation", result.title());
        verify(accommodationMapper).toEntity(createAccommodationDTO);
        verify(accommodationRepository).save(any(AccommodationEntity.class));
        verify(accommodationMapper).toCreateAccommodationResponseDTO(testAccommodationEntity);
    }

    @Test
    void create_WithNullHostId_Success() {
        // Arrange
        CreateAccommodationResponseDTO expectedResponse = new CreateAccommodationResponseDTO(
                1L, null, "Test Accommodation", "Test Description", null,
                "123 Test Street", null, new BigDecimal("100.00"),
                List.of("WiFi", "Pool"), 4, "2024-01-01T10:00:00"
        );
        when(accommodationMapper.toEntity(createAccommodationDTO)).thenReturn(testAccommodationEntity);
        when(accommodationRepository.save(any(AccommodationEntity.class))).thenReturn(testAccommodationEntity);
        when(accommodationMapper.toCreateAccommodationResponseDTO(testAccommodationEntity)).thenReturn(expectedResponse);

        // Act
        CreateAccommodationResponseDTO result = accommodationService.create(createAccommodationDTO, null);

        // Assert
        assertNotNull(result);
        verify(accommodationRepository).save(any(AccommodationEntity.class));
    }

    // UPDATE TESTS
    @Test
    void update_Success() {
        // Arrange
        Long userId = 1L;
        when(accommodationRepository.existsById(1L)).thenReturn(true);
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(testAccommodationEntity));
        when(accommodationRepository.save(testAccommodationEntity)).thenReturn(testAccommodationEntity);
        when(accommodationMapper.toAccommodationDTO(testAccommodationEntity)).thenReturn(accommodationDTO);

        // Act
        AccommodationDTO result = accommodationService.update(userId, 1L, updateAccommodationDTO);

        // Assert
        assertNotNull(result);
        verify(accommodationMapper).updateEntityFromDTO(updateAccommodationDTO, testAccommodationEntity);
        verify(accommodationRepository).save(testAccommodationEntity);
    }

    @Test
    void update_WithNullId_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> accommodationService.update(1L, null, updateAccommodationDTO));
    }

    @Test
    void update_AccommodationNotFound_ThrowsNoSuchElementException() {
        // Arrange
        when(accommodationRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(NoSuchElementException.class,
                () -> accommodationService.update(1L, 1L, updateAccommodationDTO));
    }

    @Test
    void update_DeletedAccommodation_ThrowsIllegalStateException() {
        // Arrange
        testAccommodationEntity.setSoftDeleted(true);
        when(accommodationRepository.existsById(1L)).thenReturn(true);
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(testAccommodationEntity));

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> accommodationService.update(1L, 1L, updateAccommodationDTO));
    }

    @Test
    void update_NotOwner_ThrowsSecurityException() {
        // Arrange
        Long wrongUserId = 999L;
        when(accommodationRepository.existsById(1L)).thenReturn(true);
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(testAccommodationEntity));

        // Act & Assert
        assertThrows(UnauthorizedException.class,
                () -> accommodationService.update(wrongUserId, 1L, updateAccommodationDTO));
    }

    // FIND BY ID TESTS
    @Test
    void findById_Success() {
        // Arrange
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(testAccommodationEntity));
        when(accommodationMapper.toAccommodationDTO(testAccommodationEntity)).thenReturn(accommodationDTO);

        // Act
        Optional<AccommodationDTO> result = accommodationService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(accommodationDTO, result.get());
    }

    @Test
    void findById_WithNullId_ReturnsEmpty() {
        // Act
        Optional<AccommodationDTO> result = accommodationService.findById(null);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findById_NotFound_ReturnsEmpty() {
        // Arrange
        when(accommodationRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<AccommodationDTO> result = accommodationService.findById(1L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findById_SoftDeleted_ReturnsEmpty() {
        // Arrange
        testAccommodationEntity.setSoftDeleted(true);
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(testAccommodationEntity));

        // Act
        Optional<AccommodationDTO> result = accommodationService.findById(1L);

        // Assert
        assertTrue(result.isEmpty());
    }

    // SEARCH TESTS
    @Test
    void search_WithoutServicesFilter_Success() {
        // Arrange
        AccommodationSearch criteria = new AccommodationSearch(1L, null, null, 2, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        Page<AccommodationEntity> entityPage = new PageImpl<>(List.of(testAccommodationEntity));

        when(accommodationRepository.search(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(entityPage);
        when(accommodationMapper.toAccommodationDTO(testAccommodationEntity)).thenReturn(accommodationDTO);

        // Act
        Page<AccommodationDTO> result = accommodationService.search(criteria, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(accommodationDTO, result.getContent().getFirst());
    }

    @Test
    void search_WithServicesFilter_Success() {
        // Arrange
        AccommodationSearch criteria = new AccommodationSearch(1L, null, null, 2, null, null, List.of("WiFi", "Pool"));
        Pageable pageable = PageRequest.of(0, 10);
        List<Long> accommodationIds = List.of(1L);
        List<AccommodationEntity> accommodations = List.of(testAccommodationEntity);

        when(accommodationRepository.findAccommodationIdsWithServices(
                eq(1L), isNull(), isNull(), eq(2), isNull(), isNull(), eq(List.of("WiFi", "Pool")), eq(2L), eq(pageable)))
                .thenReturn(accommodationIds);
        when(accommodationRepository.findByIdsWithEntityGraph(accommodationIds)).thenReturn(accommodations);
        when(accommodationMapper.toAccommodationDTO(testAccommodationEntity)).thenReturn(accommodationDTO);

        // Act
        Page<AccommodationDTO> result = accommodationService.search(criteria, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void search_WithServicesFilter_NoResults() {
        // Arrange
        AccommodationSearch criteria = new AccommodationSearch(1L, null, null, 2, null, null, List.of("WiFi"));
        Pageable pageable = PageRequest.of(0, 10);

        when(accommodationRepository.findAccommodationIdsWithServices(
                eq(1L), isNull(), isNull(), eq(2), isNull(), isNull(), eq(List.of("WiFi")), eq(1L), eq(pageable)))
                .thenReturn(Collections.emptyList());

        // Act
        Page<AccommodationDTO> result = accommodationService.search(criteria, pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void search_ThrowsSearchingAccommodationException() {
        // Arrange
        AccommodationSearch criteria = new AccommodationSearch(1L, null, null, 2, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        when(accommodationRepository.search(any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(SearchingAccommodationException.class,
                () -> accommodationService.search(criteria, pageable));
    }

    @Test
    void search_WithServicesFilter_ThrowsSearchingAccommodationException() {
        // Arrange
        AccommodationSearch criteria = new AccommodationSearch(1L, null, null, 2, null, null, List.of("WiFi"));
        Pageable pageable = PageRequest.of(0, 10);

        when(accommodationRepository.findAccommodationIdsWithServices(
                eq(1L), isNull(), isNull(), eq(2), isNull(), isNull(), eq(List.of("WiFi")), eq(1L), eq(pageable)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(SearchingAccommodationException.class,
                () -> accommodationService.search(criteria, pageable));
    }

    // DELETE TESTS
    @Test
    void delete_Success() {
        // Arrange
        Long userId = 1L;
        LocalDate today = LocalDate.now();
        when(accommodationRepository.existsById(1L)).thenReturn(true);
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(testAccommodationEntity));
        when(accommodationRepository.countFutureNonCancelledReservations(1L, today)).thenReturn(0L);
        when(accommodationRepository.save(testAccommodationEntity)).thenReturn(testAccommodationEntity);

        // Act
        accommodationService.delete(userId, 1L);

        // Assert
        verify(accommodationRepository).save(testAccommodationEntity);
        assertTrue(testAccommodationEntity.getSoftDeleted());
        assertFalse(testAccommodationEntity.getActive());
        assertNotNull(testAccommodationEntity.getDeletedAt());
    }

    @Test
    void delete_WithNullId_DoesNothing() {
        // Act
        accommodationService.delete(1L, null);

        // Assert
        verify(accommodationRepository, never()).save(any());
    }

    @Test
    void delete_NonExistingAccommodation_DoesNothing() {
        // Arrange
        when(accommodationRepository.existsById(1L)).thenReturn(false);

        // Act
        accommodationService.delete(1L, 1L);

        // Assert
        verify(accommodationRepository, never()).save(any());
    }

    @Test
    void delete_NotOwner_ThrowsSecurityException() {
        // Arrange
        Long wrongUserId = 999L;
        when(accommodationRepository.existsById(1L)).thenReturn(true);
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(testAccommodationEntity));

        // Act & Assert
        assertThrows(UnauthorizedException.class,
                () -> accommodationService.delete(wrongUserId, 1L));
    }

    @Test
    void delete_WithFutureReservations_ThrowsIllegalStateException() {
        // Arrange
        Long userId = 1L;
        LocalDate today = LocalDate.now();
        when(accommodationRepository.existsById(1L)).thenReturn(true);
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(testAccommodationEntity));
        when(accommodationRepository.countFutureNonCancelledReservations(1L, today)).thenReturn(1L);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> accommodationService.delete(userId, 1L));
    }

    @Test
    void delete_FallbackValidation_WithFutureReservations_ThrowsIllegalStateException() {
        // Arrange
        Long userId = 1L;
        LocalDate today = LocalDate.now();
        ReservationEntity futureReservation = new ReservationEntity();
        futureReservation.setStartDate(LocalDate.now().plusDays(10));
        testAccommodationEntity.setReservations(List.of(futureReservation));

        when(accommodationRepository.existsById(1L)).thenReturn(true);
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(testAccommodationEntity));
        when(accommodationRepository.countFutureNonCancelledReservations(1L, today))
                .thenThrow(new RuntimeException("Method not available"));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> accommodationService.delete(userId, 1L));
    }


    // FIND BY HOST TESTS
    @Test
    void findByHost_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<AccommodationEntity> entityPage = new PageImpl<>(List.of(testAccommodationEntity));

        when(accommodationRepository.findByHostIdAndSoftDeletedFalse(1L, pageable)).thenReturn(entityPage);
        when(accommodationMapper.toAccommodationDTO(testAccommodationEntity)).thenReturn(accommodationDTO);

        // Act
        Page<AccommodationDTO> result = accommodationService.findByHost(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void findByHost_WithNullHostId_ReturnsEmpty() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<AccommodationDTO> result = accommodationService.findByHost(null, pageable);

        // Assert
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void findByHost_RepositoryMethodNotAvailable_FallsBackToFindAll() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<AccommodationEntity> entityPage = new PageImpl<>(List.of(testAccommodationEntity));

        when(accommodationRepository.findByHostIdAndSoftDeletedFalse(1L, pageable))
                .thenThrow(new RuntimeException("Method not available"));
        when(accommodationRepository.findAll(pageable)).thenReturn(entityPage);
        when(accommodationMapper.toAccommodationDTO(testAccommodationEntity)).thenReturn(accommodationDTO);

        // Act
        Page<AccommodationDTO> result = accommodationService.findByHost(1L, pageable);

        // Assert
        assertNotNull(result);
        verify(accommodationRepository).findAll(pageable);
    }

    // GET METRICS TESTS
    @Test
    void getMetrics_Success() {
        // Arrange
        CommentEntity comment = new CommentEntity();
        comment.setRating(5);

        ReservationEntity reservation = new ReservationEntity();
        reservation.setStartDate(LocalDate.now().minusDays(10));
        reservation.setEndDate(LocalDate.now().minusDays(5));
        reservation.setTotalPrice(new BigDecimal("500.00"));

        testAccommodationEntity.setComments(List.of(comment));
        testAccommodationEntity.setReservations(List.of(reservation));

        when(accommodationRepository.findByIdWithReservations(1L)).thenReturn(Optional.of(testAccommodationEntity));
        when(accommodationRepository.findByIdWithComments(1L)).thenReturn(Optional.of(testAccommodationEntity));

        // Act
        AccommodationMetrics result = accommodationService.getMetrics(1L, LocalDate.now().minusDays(30), LocalDate.now());

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.totalReservations());
        assertEquals(5.0, result.averageRating());
        assertEquals(new BigDecimal("500.00"), result.totalRevenue());
    }

    @Test
    void getMetrics_AccommodationNotFound_ThrowsNoSuchElementException() {
        // Arrange
        when(accommodationRepository.findByIdWithReservations(1L)).thenReturn(Optional.empty());
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();

        // Act & Assert
        assertThrows(NoSuchElementException.class,
                () -> accommodationService.getMetrics(1L, startDate, endDate));
    }

    @Test
    void getMetrics_NoReservationsOrComments_ReturnsZeroMetrics() {
        // Arrange
        testAccommodationEntity.setComments(Collections.emptyList());
        testAccommodationEntity.setReservations(Collections.emptyList());

        when(accommodationRepository.findByIdWithReservations(1L)).thenReturn(Optional.of(testAccommodationEntity));
        when(accommodationRepository.findByIdWithComments(1L)).thenReturn(Optional.of(testAccommodationEntity));

        // Act
        AccommodationMetrics result = accommodationService.getMetrics(1L, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(0L, result.totalReservations());
        assertEquals(0.0, result.averageRating());
        assertEquals(BigDecimal.ZERO, result.totalRevenue());
    }

    // IMAGE UPLOAD TESTS
    @Test
    void uploadAndAddImages_Success() throws UploadingStorageException {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        List<MultipartFile> files = List.of(mockFile);

        Map<Object, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://example.com/image.jpg");
        uploadResult.put("public_id", "test_public_id");
        uploadResult.put("eager", "https://example.com/thumbnail.jpg");

        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(testAccommodationEntity));
        when(storageService.upload(mockFile)).thenReturn(uploadResult);
        when(accommodationRepository.save(testAccommodationEntity)).thenReturn(testAccommodationEntity);

        // Act
        List<String> result = accommodationService.uploadAndAddImages(1L, 1L, files, true);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("https://example.com/image.jpg", result.getFirst());
        verify(storageService).upload(mockFile);
    }

    @Test
    void uploadAndAddImages_EmptyFileList_ReturnsEmptyList() throws UploadingStorageException {
        // Act
        List<String> result = accommodationService.uploadAndAddImages(1L, 1L, Collections.emptyList(), false);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void uploadAndAddImages_NullFileList_ReturnsEmptyList() throws UploadingStorageException {
        // Act
        List<String> result = accommodationService.uploadAndAddImages(1L, 1L, null, false);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void uploadAndAddImages_MaxImagesReached_ThrowsIllegalStateException() {
        // Arrange
        List<ImageEntity> existingImages = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            existingImages.add(new ImageEntity());
        }
        testAccommodationEntity.setImages(existingImages);

        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(testAccommodationEntity));

        MultipartFile mockFile = mock(MultipartFile.class);
        List<MultipartFile> files = List.of(mockFile);

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> accommodationService.uploadAndAddImages(1L, 1L, files, false));
    }

    @Test
    void uploadAndAddImages_DeletedAccommodation_ThrowsIllegalStateException() {
        // Arrange
        testAccommodationEntity.setSoftDeleted(true);
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(testAccommodationEntity));

        MultipartFile mockFile = mock(MultipartFile.class);
        List<MultipartFile> files = List.of(mockFile);

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> accommodationService.uploadAndAddImages(1L, 1L, files, false));
    }

    @Test
    void uploadAndAddImages_UploadFails_ThrowsUploadingStorageException() throws UploadingStorageException {
        // Arrange
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(testAccommodationEntity));
        MultipartFile mockFile = mock(MultipartFile.class);
        List<MultipartFile> files = List.of(mockFile);
        when(storageService.upload(mockFile)).thenThrow(new UploadingStorageException("Upload failed"));

        // Act & Assert
        assertThrows(UploadingStorageException.class,
                () -> accommodationService.uploadAndAddImages(1L, 1L, files, false));
    }

    // IMAGE DELETE TESTS
    @Test
    void deleteImageFromCloudinary_Success() throws DeletingStorageException {
        // Arrange
        ImageEntity image = new ImageEntity();
        image.setId(1L);
        image.setCloudinaryPublicId("test_public_id");
        image.setAccommodation(testAccommodationEntity);

        when(accommodationRepository.existsByIdAndSoftDeletedFalse(1L)).thenReturn(true);
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(testAccommodationEntity));
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));

        // Act
        accommodationService.deleteImageFromCloudinary(1L, 1L, 1L);

        // Assert
        verify(storageService).delete("test_public_id");
        verify(imageRepository).delete(image);
    }

    @Test
    void deleteImageFromCloudinary_NullImageId_ThrowsDeletingStorageException() {
        // Act & Assert
        assertThrows(DeletingStorageException.class,
                () -> accommodationService.deleteImageFromCloudinary(1L, 1L, null));
    }

    @Test
    void deleteImageFromCloudinary_AccommodationNotFound_ThrowsDeletingStorageException() {
        // Arrange
        when(accommodationRepository.existsByIdAndSoftDeletedFalse(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(DeletingStorageException.class,
                () -> accommodationService.deleteImageFromCloudinary(1L, 1L, 1L));
    }

    @Test
    void deleteImageFromCloudinary_ImageNotFound_ThrowsDeletingStorageException() {
        // Arrange
        when(accommodationRepository.existsByIdAndSoftDeletedFalse(1L)).thenReturn(true);
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(testAccommodationEntity));
        when(imageRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DeletingStorageException.class,
                () -> accommodationService.deleteImageFromCloudinary(1L, 1L, 1L));
    }

    @Test
    void deleteImageFromCloudinary_ImageBelongsToOtherAccommodation_ThrowsDeletingStorageException() {
        // Arrange
        AccommodationEntity otherAccommodation = new AccommodationEntity();
        otherAccommodation.setId(2L);

        ImageEntity image = new ImageEntity();
        image.setId(1L);
        image.setAccommodation(otherAccommodation);

        when(accommodationRepository.existsByIdAndSoftDeletedFalse(1L)).thenReturn(true);
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(testAccommodationEntity));
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));

        // Act & Assert
        assertThrows(DeletingStorageException.class,
                () -> accommodationService.deleteImageFromCloudinary(1L, 1L, 1L));
    }

    @Test
    void deleteImageFromCloudinary_StorageDeleteFails_ThrowsDeletingStorageException() throws DeletingStorageException {
        // Arrange
        ImageEntity image = new ImageEntity();
        image.setId(1L);
        image.setCloudinaryPublicId("test_public_id");
        image.setAccommodation(testAccommodationEntity);

        when(accommodationRepository.existsByIdAndSoftDeletedFalse(1L)).thenReturn(true);
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(testAccommodationEntity));
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));
        doThrow(new DeletingStorageException("Delete failed")).when(storageService).delete("test_public_id");

        // Act & Assert
        assertThrows(DeletingStorageException.class,
                () -> accommodationService.deleteImageFromCloudinary(1L, 1L, 1L));
    }

    @Test
    void deleteImageFromCloudinary_NullPublicId_Success() throws DeletingStorageException {
        // Arrange
        ImageEntity image = new ImageEntity();
        image.setId(1L);
        image.setCloudinaryPublicId(null);
        image.setAccommodation(testAccommodationEntity);

        when(accommodationRepository.existsByIdAndSoftDeletedFalse(1L)).thenReturn(true);
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(testAccommodationEntity));
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));

        // Act
        accommodationService.deleteImageFromCloudinary(1L, 1L, 1L);

        // Assert
        verify(storageService, never()).delete(any());
        verify(imageRepository).delete(image);
    }

    // ADDITIONAL TESTS FOR COMPLETE COVERAGE

    @Test
    void getMetrics_WithDateRange_Success() {
        // Arrange
        CommentEntity comment1 = new CommentEntity();
        comment1.setRating(5);
        CommentEntity comment2 = new CommentEntity();
        comment2.setRating(3);

        ReservationEntity reservationInRange = new ReservationEntity();
        reservationInRange.setStartDate(LocalDate.now().minusDays(10));
        reservationInRange.setEndDate(LocalDate.now().minusDays(5));
        reservationInRange.setTotalPrice(new BigDecimal("300.00"));

        ReservationEntity reservationOutOfRange = new ReservationEntity();
        reservationOutOfRange.setStartDate(LocalDate.now().minusDays(50));
        reservationOutOfRange.setEndDate(LocalDate.now().minusDays(45));
        reservationOutOfRange.setTotalPrice(new BigDecimal("200.00"));

        testAccommodationEntity.setComments(List.of(comment1, comment2));
        testAccommodationEntity.setReservations(List.of(reservationInRange, reservationOutOfRange));

        when(accommodationRepository.findByIdWithReservations(1L)).thenReturn(Optional.of(testAccommodationEntity));
        when(accommodationRepository.findByIdWithComments(1L)).thenReturn(Optional.of(testAccommodationEntity));

        // Act
        AccommodationMetrics result = accommodationService.getMetrics(1L, LocalDate.now().minusDays(30), LocalDate.now());

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.totalReservations());
        assertEquals(4.0, result.averageRating());
        assertEquals(new BigDecimal("300.00"), result.totalRevenue());
    }

    @Test
    void getMetrics_WithNullRating_Success() {
        // Arrange
        CommentEntity commentWithNullRating = new CommentEntity();
        commentWithNullRating.setRating(null);
        CommentEntity commentWithRating = new CommentEntity();
        commentWithRating.setRating(4);

        testAccommodationEntity.setComments(List.of(commentWithNullRating, commentWithRating));
        testAccommodationEntity.setReservations(Collections.emptyList());

        when(accommodationRepository.findByIdWithReservations(1L)).thenReturn(Optional.of(testAccommodationEntity));
        when(accommodationRepository.findByIdWithComments(1L)).thenReturn(Optional.of(testAccommodationEntity));

        // Act
        AccommodationMetrics result = accommodationService.getMetrics(1L, null, null);

        // Assert
        assertEquals(2.0, result.averageRating()); // (0 + 4) / 2
    }

    @Test
    void search_WithNullCriteria_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<AccommodationEntity> entityPage = new PageImpl<>(List.of(testAccommodationEntity));

        when(accommodationRepository.search(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(entityPage);
        when(accommodationMapper.toAccommodationDTO(testAccommodationEntity)).thenReturn(accommodationDTO);

        // Act
        Page<AccommodationDTO> result = accommodationService.search(null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void uploadAndAddImages_WithPrimaryImage_SetsPrimaryCorrectly() throws UploadingStorageException {
        // Arrange
        ImageEntity existingImage = new ImageEntity();
        existingImage.setIsPrimary(true);
        testAccommodationEntity.setImages(new ArrayList<>(List.of(existingImage)));

        MultipartFile mockFile = mock(MultipartFile.class);
        List<MultipartFile> files = List.of(mockFile);

        Map<Object, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://example.com/image.jpg");
        uploadResult.put("public_id", "test_public_id");
        uploadResult.put("eager", "https://example.com/thumbnail.jpg");

        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(testAccommodationEntity));
        when(storageService.upload(mockFile)).thenReturn(uploadResult);
        when(accommodationRepository.save(testAccommodationEntity)).thenReturn(testAccommodationEntity);

        // Act
        List<String> result = accommodationService.uploadAndAddImages(1L, 1L, files, true);

        // Assert
        assertNotNull(result);
        assertFalse(existingImage.getIsPrimary()); // Should be set to false
        verify(accommodationRepository).save(testAccommodationEntity);
    }

    @Test
    void uploadAndAddImages_AccommodationNotFound_ThrowsNoSuchElementException() {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        List<MultipartFile> files = List.of(mockFile);

        when(accommodationRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class,
                () -> accommodationService.uploadAndAddImages(1L, 1L, files, false));
    }

    @Test
    void getMetrics_ReservationWithNullDates_FiltersOut() {
        // Arrange
        ReservationEntity validReservation = new ReservationEntity();
        validReservation.setStartDate(LocalDate.now().minusDays(10));
        validReservation.setEndDate(LocalDate.now().minusDays(5));
        validReservation.setTotalPrice(new BigDecimal("300.00"));

        ReservationEntity invalidReservation = new ReservationEntity();
        invalidReservation.setStartDate(null);
        invalidReservation.setEndDate(null);
        invalidReservation.setTotalPrice(new BigDecimal("200.00"));

        testAccommodationEntity.setReservations(List.of(validReservation, invalidReservation));
        testAccommodationEntity.setComments(Collections.emptyList());

        when(accommodationRepository.findByIdWithReservations(1L)).thenReturn(Optional.of(testAccommodationEntity));
        when(accommodationRepository.findByIdWithComments(1L)).thenReturn(Optional.of(testAccommodationEntity));

        // Act
        AccommodationMetrics result = accommodationService.getMetrics(1L, null, null);

        // Assert
        assertEquals(1L, result.totalReservations());
        assertEquals(new BigDecimal("300.00"), result.totalRevenue());
    }

    @Test
    void getMetrics_ReservationWithNullPrice_HandlesGracefully() {
        // Arrange
        ReservationEntity reservationWithNullPrice = new ReservationEntity();
        reservationWithNullPrice.setStartDate(LocalDate.now().minusDays(10));
        reservationWithNullPrice.setEndDate(LocalDate.now().minusDays(5));
        reservationWithNullPrice.setTotalPrice(null);

        testAccommodationEntity.setReservations(List.of(reservationWithNullPrice));
        testAccommodationEntity.setComments(Collections.emptyList());

        when(accommodationRepository.findByIdWithReservations(1L)).thenReturn(Optional.of(testAccommodationEntity));
        when(accommodationRepository.findByIdWithComments(1L)).thenReturn(Optional.of(testAccommodationEntity));

        // Act
        AccommodationMetrics result = accommodationService.getMetrics(1L, null, null);

        // Assert
        assertEquals(1L, result.totalReservations());
        assertEquals(BigDecimal.ZERO, result.totalRevenue());
    }

    @Test
    void getMetrics_WithOnlyStartDate_FiltersCorrectly() {
        // Arrange
        ReservationEntity validReservation = new ReservationEntity();
        validReservation.setStartDate(LocalDate.now().minusDays(5));
        validReservation.setEndDate(LocalDate.now().minusDays(2));
        validReservation.setTotalPrice(new BigDecimal("300.00"));

        ReservationEntity oldReservation = new ReservationEntity();
        oldReservation.setStartDate(LocalDate.now().minusDays(50));
        oldReservation.setEndDate(LocalDate.now().minusDays(45));
        oldReservation.setTotalPrice(new BigDecimal("200.00"));

        testAccommodationEntity.setReservations(List.of(validReservation, oldReservation));
        testAccommodationEntity.setComments(Collections.emptyList());

        when(accommodationRepository.findByIdWithReservations(1L)).thenReturn(Optional.of(testAccommodationEntity));
        when(accommodationRepository.findByIdWithComments(1L)).thenReturn(Optional.of(testAccommodationEntity));

        // Act - only start date
        AccommodationMetrics result = accommodationService.getMetrics(1L, LocalDate.now().minusDays(10), null);

        // Assert
        assertEquals(1L, result.totalReservations());
        assertEquals(new BigDecimal("300.00"), result.totalRevenue());
    }

    @Test
    void getMetrics_WithOnlyEndDate_FiltersCorrectly() {
        // Arrange
        ReservationEntity validReservation = new ReservationEntity();
        validReservation.setStartDate(LocalDate.now().minusDays(10));
        validReservation.setEndDate(LocalDate.now().minusDays(5));
        validReservation.setTotalPrice(new BigDecimal("300.00"));

        ReservationEntity futureReservation = new ReservationEntity();
        futureReservation.setStartDate(LocalDate.now().plusDays(5));
        futureReservation.setEndDate(LocalDate.now().plusDays(10));
        futureReservation.setTotalPrice(new BigDecimal("200.00"));

        testAccommodationEntity.setReservations(List.of(validReservation, futureReservation));
        testAccommodationEntity.setComments(Collections.emptyList());

        when(accommodationRepository.findByIdWithReservations(1L)).thenReturn(Optional.of(testAccommodationEntity));
        when(accommodationRepository.findByIdWithComments(1L)).thenReturn(Optional.of(testAccommodationEntity));

        // Act - only end date
        AccommodationMetrics result = accommodationService.getMetrics(1L, null, LocalDate.now());

        // Assert
        assertEquals(1L, result.totalReservations());
        assertEquals(new BigDecimal("300.00"), result.totalRevenue());
    }

    @Test
    void delete_FallbackValidation_WithNullReservations_Success() {
        // Arrange
        Long userId = 1L;
        testAccommodationEntity.setReservations(null);
        LocalDate today = LocalDate.now();

        when(accommodationRepository.existsById(1L)).thenReturn(true);
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(testAccommodationEntity));
        when(accommodationRepository.countFutureNonCancelledReservations(1L, today))
                .thenThrow(new RuntimeException("Method not available"));
        when(accommodationRepository.save(testAccommodationEntity)).thenReturn(testAccommodationEntity);

        // Act
        accommodationService.delete(userId, 1L);

        // Assert
        verify(accommodationRepository).save(testAccommodationEntity);
        assertTrue(testAccommodationEntity.getSoftDeleted());
    }

    @Test
    void delete_FallbackValidation_WithEmptyReservations_Success() {
        // Arrange
        Long userId = 1L;
        testAccommodationEntity.setReservations(Collections.emptyList());
        LocalDate today = LocalDate.now();

        when(accommodationRepository.existsById(1L)).thenReturn(true);
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(testAccommodationEntity));
        when(accommodationRepository.countFutureNonCancelledReservations(1L, today))
                .thenThrow(new RuntimeException("Method not available"));
        when(accommodationRepository.save(testAccommodationEntity)).thenReturn(testAccommodationEntity);

        // Act
        accommodationService.delete(userId, 1L);

        // Assert
        verify(accommodationRepository).save(testAccommodationEntity);
        assertTrue(testAccommodationEntity.getSoftDeleted());
    }

    @Test
    void getMetrics_CommentsNotFoundThrowsException_CommentsWithRating() {
        // Arrange
        when(accommodationRepository.findByIdWithReservations(1L)).thenReturn(Optional.of(testAccommodationEntity));
        when(accommodationRepository.findByIdWithComments(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class,
                () -> accommodationService.getMetrics(1L, null, null));
    }

    @Test
    void search_FiltersSoftDeletedAccommodations() {
        // Arrange
        AccommodationEntity softDeletedAccommodation = new AccommodationEntity();
        softDeletedAccommodation.setId(2L);
        softDeletedAccommodation.setSoftDeleted(true);

        AccommodationSearch criteria = new AccommodationSearch(1L, null, null, 2, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        Page<AccommodationEntity> entityPage = new PageImpl<>(List.of(testAccommodationEntity, softDeletedAccommodation));

        when(accommodationRepository.search(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(entityPage);
        when(accommodationMapper.toAccommodationDTO(testAccommodationEntity)).thenReturn(accommodationDTO);

        // Act
        Page<AccommodationDTO> result = accommodationService.search(criteria, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size()); // Only non-deleted accommodation
        assertEquals(accommodationDTO, result.getContent().getFirst());
    }

    @Test
    void findByHost_FiltersByHostMatch() {
        // Arrange
        AccommodationEntity differentHostAccommodation = new AccommodationEntity();
        differentHostAccommodation.setId(2L);
        UserEntity differentHost = new UserEntity();
        differentHost.setId(2L);
        differentHostAccommodation.setHost(differentHost);

        Pageable pageable = PageRequest.of(0, 10);
        Page<AccommodationEntity> entityPage = new PageImpl<>(List.of(testAccommodationEntity, differentHostAccommodation));

        when(accommodationRepository.findByHostIdAndSoftDeletedFalse(1L, pageable)).thenReturn(entityPage);
        when(accommodationMapper.toAccommodationDTO(testAccommodationEntity)).thenReturn(accommodationDTO);

        // Act
        Page<AccommodationDTO> result = accommodationService.findByHost(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size()); // Only matching host
    }

    @Test
    void create_SetsDefaultValues() {
        // Arrange
        AccommodationEntity entityWithNullDefaults = new AccommodationEntity();
        entityWithNullDefaults.setId(1L);
        entityWithNullDefaults.setTitle("Test");
        entityWithNullDefaults.setActive(null);
        entityWithNullDefaults.setSoftDeleted(null);

        CreateAccommodationResponseDTO expectedResponse = new CreateAccommodationResponseDTO(
                1L, 1L, "Test", "Description", null,
                "Address", null, new BigDecimal("100.00"),
                List.of(), 1, "2024-01-01T10:00:00"
        );

        when(accommodationMapper.toEntity(createAccommodationDTO)).thenReturn(entityWithNullDefaults);
        when(accommodationRepository.save(any(AccommodationEntity.class))).thenReturn(entityWithNullDefaults);
        when(accommodationMapper.toCreateAccommodationResponseDTO(entityWithNullDefaults)).thenReturn(expectedResponse);

        // Act
        CreateAccommodationResponseDTO result = accommodationService.create(createAccommodationDTO, 1L);

        // Assert
        assertNotNull(result);
        // Verify that default values were set during save
        verify(accommodationRepository).save(argThat(entity ->
                entity.getActive() && !entity.getSoftDeleted()));
    }
}
