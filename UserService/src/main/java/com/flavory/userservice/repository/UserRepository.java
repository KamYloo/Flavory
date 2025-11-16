package com.flavory.userservice.repository;

import com.flavory.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByAuth0Id(String auth0Id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByAuth0Id(String auth0Id);
}
