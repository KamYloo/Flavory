package com.flavory.dishservice.repository;

import com.flavory.dishservice.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {
    @Query("SELECT COUNT(d) FROM Dish d WHERE d.cookId = :cookId AND d.isActive = true")
    Long countActiveDishesForCook(@Param("cookId") String cookId);

    boolean existsByCookIdAndName(String cookId, String name);

    Optional<Dish> findByIdAndIsActiveTrue(Long id);
}

