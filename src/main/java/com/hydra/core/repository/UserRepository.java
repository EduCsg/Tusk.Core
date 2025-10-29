package com.hydra.core.repository;

import com.hydra.core.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

	Optional<UserEntity> findByEmailOrUsername(String email, String username);

}