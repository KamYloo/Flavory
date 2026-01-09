package com.flavory.dishservice.repository;

import com.flavory.dishservice.entity.CookProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CookProfileRepository extends JpaRepository<CookProfile, String> {
}
