//package com.avanzada.alojamientos.repositories;
//
//import com.avanzada.alojamientos.entities.CityEntity;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//class CityRepositoryTest {
//
//    @Autowired
//    private CityRepository cityRepository;
//
//    @Test
//    void testFindById_ShouldReturnCity() {
//        // Given: ID de ciudad existente
//        Long cityId = 1L;
//
//        // When: Buscar por ID
//        Optional<CityEntity> result = cityRepository.findById(cityId);
//
//        // Then: Debe encontrar la ciudad
//        assertTrue(result.isPresent(), "La ciudad debe existir");
//        assertEquals("Medellín", result.get().getName());
//        assertEquals("Colombia", result.get().getCountry());
//    }
//
//    @Test
//    void testFindByNameAndCountry() {
//        // Given: Nombre y país existentes
//        String name = "Bogotá";
//        String country = "Colombia";
//
//        // When: Buscar por nombre y país
//        Optional<CityEntity> result = cityRepository.findByNameAndCountry(name, country);
//
//        // Then: Debe encontrar la ciudad
//        assertTrue(result.isPresent(), "La ciudad debe existir");
//        assertEquals(name, result.get().getName());
//        assertEquals(country, result.get().getCountry());
//    }
//
//    @Test
//    void testFindByNameAndCountry_NotFound() {
//        // Given: Nombre y país no existentes
//        String name = "Madrid";
//        String country = "España";
//
//        // When: Buscar por nombre y país
//        Optional<CityEntity> result = cityRepository.findByNameAndCountry(name, country);
//
//        // Then: No debe encontrar nada (aunque Barcelona está en España)
//        assertFalse(result.isPresent(), "No debe encontrar Madrid");
//    }
//
//    @Test
//    void testExistsByNameAndCountry() {
//        // Given: Nombre y país existentes
//        String name = "Cartagena";
//        String country = "Colombia";
//
//        // When: Verificar existencia
//        boolean exists = cityRepository.existsByNameAndCountry(name, country);
//
//        // Then: Debe retornar true
//        assertTrue(exists, "Cartagena debe existir");
//    }
//
//    @Test
//    void testFindByCountry() {
//        // Given: País con múltiples ciudades
//        String country = "Colombia";
//
//        // When: Buscar ciudades por país
//        List<CityEntity> result = cityRepository.findByCountry(country);
//
//        // Then: Debe encontrar al menos 4 ciudades
//        assertTrue(result.size() >= 4, "Colombia debe tener al menos 4 ciudades");
//        assertTrue(result.stream().allMatch(c -> c.getCountry().equals(country)),
//                "Todas las ciudades deben ser de Colombia");
//    }
//
//    @Test
//    void testFindAll() {
//        // When: Buscar todas las ciudades
//        List<CityEntity> result = cityRepository.findAll();
//
//        // Then: Debe haber al menos 5 ciudades
//        assertTrue(result.size() >= 5, "Debe haber al menos 5 ciudades");
//
//        // Verificar que están todas las ciudades esperadas
//        List<String> cityNames = result.stream().map(CityEntity::getName).toList();
//        assertTrue(cityNames.contains("Medellín"), "Debe incluir Medellín");
//        assertTrue(cityNames.contains("Bogotá"), "Debe incluir Bogotá");
//        assertTrue(cityNames.contains("Cartagena"), "Debe incluir Cartagena");
//        assertTrue(cityNames.contains("Cali"), "Debe incluir Cali");
//        assertTrue(cityNames.contains("Barcelona"), "Debe incluir Barcelona");
//    }
//
//    @Test
//    void testFindByCountry_Spain() {
//        // Given: País España
//        String country = "España";
//
//        // When: Buscar ciudades de España
//        List<CityEntity> result = cityRepository.findByCountry(country);
//
//        // Then: Debe encontrar al menos 1 ciudad
//        assertFalse(result.isEmpty(), "España debe tener al menos 1 ciudad");
//        assertTrue(result.stream().anyMatch(c -> c.getName().equals("Barcelona")),
//                "Debe incluir Barcelona");
//    }
//
//    @Test
//    void testCityWithAccommodations() {
//        // Given: ID de ciudad con alojamientos
//        Long cityId = 1L; // Medellín
//
//        // When: Buscar la ciudad
//        Optional<CityEntity> result = cityRepository.findById(cityId);
//
//        // Then: Debe tener alojamientos
//        assertTrue(result.isPresent(), "La ciudad debe existir");
//        assertNotNull(result.get().getAccommodations(), "Debe tener lista de alojamientos");
//    }
//
//    @Test
//    void testSearchCitiesByNameContaining() {
//        // Given: Parte del nombre de una ciudad
//        String searchTerm = "Bog";
//
//        // When: Buscar ciudades que contengan ese término
//        List<CityEntity> result = cityRepository.findByNameContainingIgnoreCase(searchTerm);
//
//        // Then: Debe encontrar Bogotá
//        assertFalse(result.isEmpty(), "Debe encontrar ciudades");
//        assertTrue(result.stream().anyMatch(c -> c.getName().equals("Bogotá")),
//                "Debe encontrar Bogotá");
//    }
//
//    @Test
//    void testFindCitiesWithAccommodationsCount() {
//        // When: Buscar todas las ciudades
//        List<CityEntity> cities = cityRepository.findAll();
//
//        // Then: Al menos algunas deben tener alojamientos
//        long citiesWithAccommodations = cities.stream()
//                .filter(c -> c.getAccommodations() != null && !c.getAccommodations().isEmpty())
//                .count();
//
//        assertTrue(citiesWithAccommodations > 0,
//                "Al menos una ciudad debe tener alojamientos");
//    }
//}
//
