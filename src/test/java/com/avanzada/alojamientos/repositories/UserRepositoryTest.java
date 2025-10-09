//package com.avanzada.alojamientos.repositories;
//
//import com.avanzada.alojamientos.entities.UserEntity;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//class UserRepositoryTest {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Test
//    void testFindByEmail_ShouldReturnUser() {
//        // Given: Email existente en import.sql
//        String email = "maria@test.com";
//
//        // When: Buscar por email
//        Optional<UserEntity> result = userRepository.findByEmail(email);
//
//        // Then: Debe encontrar el usuario
//        assertTrue(result.isPresent(), "El usuario debe existir");
//        assertEquals("María González", result.get().getName());
//        assertEquals(email, result.get().getEmail());
//    }
//
//    @Test
//    void testFindByEmail_NotFound() {
//        // Given: Email no existente
//        String email = "noexiste@test.com";
//
//        // When: Buscar por email
//        Optional<UserEntity> result = userRepository.findByEmail(email);
//
//        // Then: No debe encontrar nada
//        assertFalse(result.isPresent(), "No debe encontrar el usuario");
//    }
//
//    @Test
//    void testExistsByEmail_ShouldReturnTrue() {
//        // Given: Email existente
//        String email = "carlos@test.com";
//
//        // When: Verificar existencia
//        boolean exists = userRepository.existsByEmail(email);
//
//        // Then: Debe retornar true
//        assertTrue(exists, "El email debe existir");
//    }
//
//    @Test
//    void testExistsByEmail_ShouldReturnFalse() {
//        // Given: Email no existente
//        String email = "inventado@test.com";
//
//        // When: Verificar existencia
//        boolean exists = userRepository.existsByEmail(email);
//
//        // Then: Debe retornar false
//        assertFalse(exists, "El email no debe existir");
//    }
//
//    @Test
//    void testFindById_WithRelations() {
//        // Given: ID de usuario existente
//        Long userId = 2L; // María González
//
//        // When: Buscar por ID
//        Optional<UserEntity> result = userRepository.findById(userId);
//
//        // Then: Debe encontrar el usuario con sus relaciones
//        assertTrue(result.isPresent(), "El usuario debe existir");
//        UserEntity user = result.get();
//
//        assertNotNull(user.getRoles(), "Los roles deben estar cargados");
//        assertFalse(user.getRoles().isEmpty(), "Debe tener al menos un rol");
//        assertTrue(user.getVerified(), "María debe estar verificada");
//        assertTrue(user.getEnabled(), "María debe estar habilitada");
//    }
//
//    @Test
//    void testFindByIdWithHostProfile() {
//        // Given: ID de usuario que es host
//        Long userId = 2L; // María González es host
//
//        // When: Buscar con perfil de host
//        Optional<UserEntity> result = userRepository.findByIdWithHostProfile(userId);
//
//        // Then: Debe tener el perfil de host cargado
//        assertTrue(result.isPresent(), "El usuario debe existir");
//        assertNotNull(result.get().getHostProfile(), "El perfil de host debe estar cargado");
//        assertEquals("Alojamientos María", result.get().getHostProfile().getBusinessName());
//    }
//
//    @Test
//    void testFindByIdWithHostProfile_NotHost() {
//        // Given: ID de usuario que NO es host
//        Long userId = 5L; // Laura Pérez solo es USER
//
//        // When: Buscar con perfil de host
//        Optional<UserEntity> result = userRepository.findByIdWithHostProfile(userId);
//
//        // Then: No debe tener perfil de host
//        assertTrue(result.isPresent(), "El usuario debe existir");
//        assertNull(result.get().getHostProfile(), "No debe tener perfil de host");
//    }
//
//    @Test
//    void testFindByIdWithReservations() {
//        // Given: ID de usuario con reservaciones
//        Long userId = 5L; // Laura Pérez tiene reservaciones
//
//        // When: Buscar con reservaciones
//        Optional<UserEntity> result = userRepository.findByIdWithReservations(userId);
//
//        // Then: Debe tener reservaciones cargadas
//        assertTrue(result.isPresent(), "El usuario debe existir");
//        assertNotNull(result.get().getReservations(), "Las reservaciones deben estar cargadas");
//        assertFalse(result.get().getReservations().isEmpty(), "Debe tener al menos una reservación");
//    }
//
//    @Test
//    void testFindByIdWithFavorites() {
//        // Given: ID de usuario con favoritos
//        Long userId = 5L; // Laura Pérez tiene favoritos
//
//        // When: Buscar con favoritos
//        Optional<UserEntity> result = userRepository.findByIdWithFavorites(userId);
//
//        // Then: Debe tener favoritos cargados
//        assertTrue(result.isPresent(), "El usuario debe existir");
//        assertNotNull(result.get().getFavorites(), "Los favoritos deben estar cargados");
//        assertFalse(result.get().getFavorites().isEmpty(), "Debe tener al menos un favorito");
//    }
//
//    @Test
//    void testUserWithMultipleRoles() {
//        // Given: ID de usuario con múltiples roles
//        Long userId = 2L; // María tiene USER y HOST
//
//        // When: Buscar usuario
//        Optional<UserEntity> result = userRepository.findById(userId);
//
//        // Then: Debe tener múltiples roles
//        assertTrue(result.isPresent(), "El usuario debe existir");
//        assertTrue(result.get().getRoles().size() >= 2, "Debe tener al menos 2 roles");
//        assertTrue(result.get().getRoles().contains("USER"), "Debe tener rol USER");
//        assertTrue(result.get().getRoles().contains("HOST"), "Debe tener rol HOST");
//    }
//
//    @Test
//    void testUserWithProfileImage() {
//        // Given: ID de usuario con imagen de perfil
//        Long userId = 2L; // María tiene imagen de perfil
//
//        // When: Buscar usuario
//        Optional<UserEntity> result = userRepository.findById(userId);
//
//        // Then: Debe tener imagen de perfil
//        assertTrue(result.isPresent(), "El usuario debe existir");
//        assertNotNull(result.get().getProfileImage(), "Debe tener imagen de perfil");
//    }
//
//    @Test
//    void testFindAll() {
//        // When: Buscar todos los usuarios
//        var users = userRepository.findAll();
//
//        // Then: Debe haber al menos 6 usuarios
//        assertFalse(users.isEmpty(), "Debe haber usuarios en la base de datos");
//        assertTrue(users.size() >= 6, "Debe haber al menos 6 usuarios");
//    }
//
//    @Test
//    void testDeletedFlag() {
//        // Given: Todos los usuarios deben tener deleted = false inicialmente
//        var users = userRepository.findAll();
//
//        // Then: Ninguno debe estar eliminado
//        assertTrue(users.stream().noneMatch(UserEntity::getDeleted),
//                "Ningún usuario debe estar marcado como eliminado");
//    }
//}
//
