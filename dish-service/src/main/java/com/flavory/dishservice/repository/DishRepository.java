package com.flavory.dishservice.repository;

import com.flavory.dishservice.entity.Dish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long>, JpaSpecificationExecutor<Dish> {
    boolean existsByCookIdAndName(String cookId, String name);
    boolean existsByCookIdAndNameAndIdNot(String cookId, String name, Long id);

    Optional<Dish> findByIdAndIsActiveTrue(Long id);
    Optional<Dish> findByIdAndCookId(Long id, String cookId);
    Page<Dish> findByCookIdAndIsActiveTrue(String cookId, Pageable pageable);

    @Query("SELECT d FROM Dish d WHERE d.featured = true " +
            "AND d.isActive = true AND d.available = true AND d.currentStock > 0")
    Page<Dish> findFeaturedDishes(Pageable pageable);

    @Query("SELECT d FROM Dish d WHERE d.isActive = true AND d.available = true " +
            "AND d.currentStock > 0")
    Page<Dish> findAllAvailableDishes(Pageable pageable);

    @Query("SELECT d FROM Dish d WHERE d.category = :category " +
            "AND d.isActive = true AND d.available = true AND d.currentStock > 0")
    Page<Dish> findByCategory(@Param("category") Dish.DishCategory category, Pageable pageable);

    @Query("SELECT d FROM Dish d WHERE d.isActive = true AND d.available = true " +
            "AND d.currentStock > 0 AND d.totalRatings >= 5 " +
            "ORDER BY d.averageRating DESC, d.totalRatings DESC")
    Page<Dish> findTopRatedDishes(Pageable pageable);

    @EntityGraph(attributePaths = {"images"})
    @Query("SELECT DISTINCT d FROM Dish d WHERE d.id IN :ids")
    List<Dish> findAllByIdsWithImages(@Param("ids") List<Long> ids);

    @Query("SELECT COUNT(d) FROM Dish d WHERE d.cookId = :cookId AND d.isActive = true")
    Long countActiveDishesForCook(@Param("cookId") String cookId);

    @Query("SELECT COUNT(d) FROM Dish d WHERE d.cookId = :cookId AND d.isActive = true")
    Long countTotalDishes(@Param("cookId") String cookId);

    @Query("SELECT COUNT(d) FROM Dish d WHERE d.cookId = :cookId " +
            "AND d.isActive = true AND d.available = true AND d.currentStock > 0")
    Long countAvailableDishes(@Param("cookId") String cookId);

    @Query("SELECT COALESCE(SUM(d.totalRevenue), 0) FROM Dish d WHERE d.cookId = :cookId")
    BigDecimal getTotalRevenue(@Param("cookId") String cookId);

    @Query("SELECT COALESCE(SUM(d.totalOrders), 0) FROM Dish d WHERE d.cookId = :cookId")
    Long getTotalOrders(@Param("cookId") String cookId);

    @Query("SELECT COALESCE(AVG(d.averageRating), 0) FROM Dish d " +
            "WHERE d.cookId = :cookId AND d.totalRatings > 0")
    BigDecimal getAverageRating(@Param("cookId") String cookId);

    @Query("SELECT COALESCE(AVG(d.price), 0) FROM Dish d " +
            "WHERE d.cookId = :cookId AND d.isActive = true")
    BigDecimal getAveragePrice(@Param("cookId") String cookId);
}

