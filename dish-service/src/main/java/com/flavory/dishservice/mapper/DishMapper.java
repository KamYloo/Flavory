package com.flavory.dishservice.mapper;

import com.flavory.dishservice.dto.request.CreateDishRequest;
import com.flavory.dishservice.dto.request.NutritionInfoRequest;
import com.flavory.dishservice.dto.request.UpdateDishRequest;
import com.flavory.dishservice.dto.response.DishResponse;
import com.flavory.dishservice.dto.response.NutritionInfoResponse;
import com.flavory.dishservice.entity.Dish;
import com.flavory.dishservice.entity.NutritionInfo;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DishMapper {

    @Mapping(target = "categoryDisplayName", source = "category", qualifiedByName = "mapCategoryDisplayName")
    @Mapping(target = "allergenDisplayNames", source = "allergens", qualifiedByName = "mapAllergenDisplayNames")
    DishResponse toResponse(Dish dish);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cookId", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "available", constant = "true")
    @Mapping(target = "featured", constant = "false")
    @Mapping(target = "averageRating", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "totalRatings", constant = "0")
    @Mapping(target = "totalOrders", constant = "0")
    @Mapping(target = "totalRevenue", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "deactivationReason", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Dish toEntity(CreateDishRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cookId", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "currentStock", ignore = true)
    @Mapping(target = "maxDailyStock", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "totalRatings", ignore = true)
    @Mapping(target = "totalOrders", ignore = true)
    @Mapping(target = "totalRevenue", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "deactivationReason", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromRequest(UpdateDishRequest request, @MappingTarget Dish dish);


    NutritionInfo toNutritionInfo(NutritionInfoRequest request);

    NutritionInfoResponse toNutritionInfoResponse(NutritionInfo nutritionInfo);


    @Named("mapCategoryDisplayName")
    default String mapCategoryDisplayName(Dish.DishCategory category) {
        return category != null ? category.getDisplayName() : null;
    }

    @Named("mapAllergenDisplayNames")
    default Set<String> mapAllergenDisplayNames(Set<Dish.Allergen> allergens) {
        if (allergens == null || allergens.isEmpty()) {
            return null;
        }
        return allergens.stream()
                .map(Dish.Allergen::getDisplayName)
                .collect(Collectors.toSet());
    }
}