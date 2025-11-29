package com.flavory.dishservice.specification;

import com.flavory.dishservice.entity.Dish;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DishSpecification {

    public static Specification<Dish> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("isActive"));
    }

    public static Specification<Dish> isAvailable() {
        return (root, query, cb) -> cb.and(
                cb.isTrue(root.get("available")),
                cb.greaterThan(root.get("currentStock"), 0)
        );
    }

    public static Specification<Dish> hasCookId(String cookId) {
        return (root, query, cb) ->
                cookId == null ? cb.conjunction() : cb.equal(root.get("cookId"), cookId);
    }

    public static Specification<Dish> hasCategory(Dish.DishCategory category) {
        return (root, query, cb) ->
                category == null ? cb.conjunction() : cb.equal(root.get("category"), category);
    }

    public static Specification<Dish> hasPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Dish> searchByNameOrDescription(String searchTerm) {
        return (root, query, cb) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return cb.conjunction();
            }

            String likePattern = "%" + searchTerm.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("name")), likePattern),
                    cb.like(cb.lower(root.get("description")), likePattern)
            );
        };
    }

    public static Specification<Dish> searchByTags(String searchTerm) {
        return (root, query, cb) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return cb.conjunction();
            }

            Join<Dish, String> tagsJoin = root.join("tags", JoinType.LEFT);
            String likePattern = "%" + searchTerm.toLowerCase() + "%";

            return cb.like(cb.lower(tagsJoin.as(String.class)), likePattern);
        };
    }

    public static Specification<Dish> searchGlobal(String searchTerm) {
        return (root, query, cb) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return cb.conjunction();
            }

            String likePattern = "%" + searchTerm.toLowerCase() + "%";

            Join<Dish, String> tagsJoin = root.join("tags", JoinType.LEFT);

            return cb.or(
                    cb.like(cb.lower(root.get("name")), likePattern),
                    cb.like(cb.lower(root.get("description")), likePattern),
                    cb.like(cb.lower(tagsJoin.as(String.class)), likePattern)
            );
        };
    }

    public static Specification<Dish> withoutAllergens(Set<Dish.Allergen> excludedAllergens) {
        return (root, query, cb) -> {
            if (excludedAllergens == null || excludedAllergens.isEmpty()) {
                return cb.conjunction();
            }

            var subquery = query.subquery(Long.class);
            var subRoot = subquery.from(Dish.class);
            var subAllergensJoin = subRoot.join("allergens");

            subquery.select(subRoot.get("id"))
                    .where(subAllergensJoin.in(excludedAllergens));

            return cb.not(root.get("id").in(subquery));
        };
    }

    public static Specification<Dish> hasAllergen(Dish.Allergen allergen) {
        return (root, query, cb) -> {
            if (allergen == null) {
                return cb.conjunction();
            }

            Join<Dish, Dish.Allergen> allergensJoin = root.join("allergens", JoinType.INNER);
            return cb.equal(allergensJoin, allergen);
        };
    }

    public static Specification<Dish> hasMinimumRating(BigDecimal minRating) {
        return (root, query, cb) ->
                minRating == null ? cb.conjunction() :
                        cb.greaterThanOrEqualTo(root.get("averageRating"), minRating);
    }

    public static Specification<Dish> hasMinimumRatingsCount(Integer minCount) {
        return (root, query, cb) ->
                minCount == null ? cb.conjunction() :
                        cb.greaterThanOrEqualTo(root.get("totalRatings"), minCount);
    }

    public static Specification<Dish> isFeatured() {
        return (root, query, cb) -> cb.isTrue(root.get("featured"));
    }

    public static Specification<Dish> isPopular(Integer minOrders) {
        return (root, query, cb) ->
                minOrders == null ? cb.conjunction() :
                        cb.greaterThanOrEqualTo(root.get("totalOrders"), minOrders);
    }

    public static Specification<Dish> hasStock() {
        return (root, query, cb) -> cb.greaterThan(root.get("currentStock"), 0);
    }

    public static Specification<Dish> hasLowStock(Integer threshold) {
        return (root, query, cb) -> cb.and(
                cb.greaterThan(root.get("currentStock"), 0),
                cb.lessThanOrEqualTo(root.get("currentStock"), threshold)
        );
    }

    public static Specification<Dish> isOutOfStock() {
        return (root, query, cb) -> cb.equal(root.get("currentStock"), 0);
    }

    public static Specification<Dish> hasPreparationTimeLessThan(Integer maxMinutes) {
        return (root, query, cb) ->
                maxMinutes == null ? cb.conjunction() :
                        cb.lessThanOrEqualTo(root.get("preparationTime"), maxMinutes);
    }

    public static Specification<Dish> canBeOrdered() {
        return isActive()
                .and(isAvailable())
                .and(hasStock());
    }

    public static Specification<Dish> searchWithFilters(
            String searchTerm,
            Dish.DishCategory category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Set<Dish.Allergen> excludedAllergens,
            Integer maxPreparationTime,
            Boolean onlyFeatured) {

        Specification<Dish> spec = canBeOrdered();

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            spec = spec.and(searchGlobal(searchTerm));
        }

        if (category != null) {
            spec = spec.and(hasCategory(category));
        }

        if (minPrice != null || maxPrice != null) {
            spec = spec.and(hasPriceBetween(minPrice, maxPrice));
        }

        if (excludedAllergens != null && !excludedAllergens.isEmpty()) {
            spec = spec.and(withoutAllergens(excludedAllergens));
        }

        if (maxPreparationTime != null) {
            spec = spec.and(hasPreparationTimeLessThan(maxPreparationTime));
        }

        if (Boolean.TRUE.equals(onlyFeatured)) {
            spec = spec.and(isFeatured());
        }

        return spec;
    }
}