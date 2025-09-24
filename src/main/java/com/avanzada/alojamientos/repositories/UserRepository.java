package com.avanzada.alojamientos.repositories;

import com.avanzada.alojamientos.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

}
