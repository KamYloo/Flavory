package com.flavory.dishservice.serviceTests;

import com.flavory.dishservice.dto.request.CreateDishRequest;
import com.flavory.dishservice.dto.request.UpdateDishRequest;
import com.flavory.dishservice.dto.request.UpdateStockRequest;
import com.flavory.dishservice.dto.response.DishResponse;
import com.flavory.dishservice.dto.response.DishStatsResponse;
import com.flavory.dishservice.entity.Dish;
import com.flavory.dishservice.exception.*;
import com.flavory.dishservice.mapper.DishMapper;
import com.flavory.dishservice.messaging.publisher.DishEventPublisher;
import com.flavory.dishservice.repository.DishRepository;
import com.flavory.dishservice.service.FileStorageService;
import com.flavory.dishservice.service.impl.DishServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DishServiceImpl Tests")
class DishServiceImplTest {

    @Mock private DishRepository dishRepository;
    @Mock private DishMapper dishMapper;
    @Mock private FileStorageService fileStorageService;
    @Mock private DishEventPublisher eventPublisher;

    @InjectMocks
    private DishServiceImpl dishService;

    private static final String COOK_ID = "cook123";
    private static final Long DISH_ID = 1L;
    private static final Integer MAX_DISHES = 50;

    private Dish createDish(String name, Integer stock, boolean available) {
        return Dish.builder()
                .id(DISH_ID)
                .cookId(COOK_ID)
                .name(name)
                .description("Test description")
                .price(new BigDecimal("25.00"))
                .category(Dish.DishCategory.MAIN_COURSE)
                .preparationTime(30)
                .servingSize(1)
                .currentStock(stock)
                .maxDailyStock(10)
                .available(available)
                .isActive(true)
                .averageRating(BigDecimal.ZERO)
                .totalRatings(0)
                .totalOrders(0)
                .totalRevenue(BigDecimal.ZERO)
                .allergens(Set.of())
                .tags(Set.of())
                .images(List.of())
                .build();
    }

    private Dish createActiveDish() {
        return createDish("Test Dish", 5, true);
    }

    private CreateDishRequest createDishRequest(String name) {
        return CreateDishRequest.builder()
                .name(name)
                .description("Test description with more than twenty characters")
                .price(new BigDecimal("25.00"))
                .category(Dish.DishCategory.MAIN_COURSE)
                .preparationTime(30)
                .servingSize(1)
                .currentStock(5)
                .maxDailyStock(10)
                .build();
    }

    private UpdateDishRequest updateDishRequest() {
        return UpdateDishRequest.builder()
                .name("Updated Dish")
                .description("Updated description with more than twenty characters")
                .price(new BigDecimal("30.00"))
                .build();
    }

    private DishResponse createDishResponse(String name) {
        return DishResponse.builder()
                .id(DISH_ID)
                .name(name)
                .price(new BigDecimal("25.00"))
                .available(true)
                .build();
    }

    @Nested
    @DisplayName("createDish")
    class CreateDishTests {

        @Test
        @DisplayName("Should create dish successfully")
        void shouldCreateDish() {
            ReflectionTestUtils.setField(dishService, "maxDishesPerCook", MAX_DISHES);
            CreateDishRequest request = createDishRequest("New Dish");
            Dish dish = createActiveDish();
            List<String> imageUrls = List.of("image1.jpg", "image2.jpg");

            when(dishRepository.countActiveDishesForCook(COOK_ID)).thenReturn(10L);
            when(dishRepository.existsByCookIdAndName(COOK_ID, "New Dish")).thenReturn(false);
            when(dishMapper.toEntity(request)).thenReturn(dish);
            when(dishRepository.save(any(Dish.class))).thenReturn(dish);
            when(dishMapper.toResponse(dish)).thenReturn(createDishResponse("New Dish"));

            DishResponse result = dishService.createDish(request, COOK_ID, imageUrls);

            assertThat(result.getName()).isEqualTo("New Dish");
            verify(dishRepository).save(any(Dish.class));
            verify(eventPublisher).publishDishCreated(any());
        }

        @Test
        @DisplayName("Should throw exception when max dishes limit reached")
        void shouldThrowWhenMaxDishesReached() {
            ReflectionTestUtils.setField(dishService, "maxDishesPerCook", MAX_DISHES);
            CreateDishRequest request = createDishRequest("New Dish");

            when(dishRepository.countActiveDishesForCook(COOK_ID)).thenReturn(50L);

            assertThatThrownBy(() -> dishService.createDish(request, COOK_ID, List.of()))
                    .isInstanceOf(MaxDishesLimitException.class);

            verify(dishRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when dish name exists")
        void shouldThrowWhenDishNameExists() {
            ReflectionTestUtils.setField(dishService, "maxDishesPerCook", MAX_DISHES);
            CreateDishRequest request = createDishRequest("Existing Dish");

            when(dishRepository.countActiveDishesForCook(COOK_ID)).thenReturn(10L);
            when(dishRepository.existsByCookIdAndName(COOK_ID, "Existing Dish")).thenReturn(true);

            assertThatThrownBy(() -> dishService.createDish(request, COOK_ID, List.of()))
                    .isInstanceOf(BusinessValidationException.class);

            verify(dishRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateDish")
    class UpdateDishTests {

        @Test
        @DisplayName("Should update dish without images")
        void shouldUpdateDishWithoutImages() {
            Dish dish = createActiveDish();
            UpdateDishRequest request = updateDishRequest();

            when(dishRepository.findByIdAndCookId(DISH_ID, COOK_ID)).thenReturn(Optional.of(dish));
            when(dishRepository.save(dish)).thenReturn(dish);
            when(dishMapper.toResponse(dish)).thenReturn(createDishResponse("Updated Dish"));

            DishResponse result = dishService.updateDish(DISH_ID, request, COOK_ID, null);

            assertThat(result).isNotNull();
            verify(dishRepository).save(dish);
            verify(eventPublisher).publishDishUpdated(any());
            verify(fileStorageService, never()).updateFiles(any(), any());
        }

        @Test
        @DisplayName("Should update dish with new images")
        void shouldUpdateDishWithImages() {
            Dish dish = createActiveDish();
            List<String> initialImages = dish.getImages();

            UpdateDishRequest request = updateDishRequest();
            List<MultipartFile> newImages = List.of(mock(MultipartFile.class));
            List<String> newImageUrls = List.of("new1.jpg", "new2.jpg");

            when(dishRepository.findByIdAndCookId(DISH_ID, COOK_ID)).thenReturn(Optional.of(dish));
            when(fileStorageService.updateFiles(newImages, initialImages)).thenReturn(newImageUrls);
            when(dishRepository.save(dish)).thenReturn(dish);
            when(dishMapper.toResponse(dish)).thenReturn(createDishResponse("Updated Dish"));

            dishService.updateDish(DISH_ID, request, COOK_ID, newImages);

            verify(fileStorageService).updateFiles(newImages, initialImages);
            verify(dishRepository).save(dish);
        }

        @Test
        @DisplayName("Should throw exception when dish not found")
        void shouldThrowWhenDishNotFound() {
            UpdateDishRequest request = updateDishRequest();

            when(dishRepository.findByIdAndCookId(DISH_ID, COOK_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> dishService.updateDish(DISH_ID, request, COOK_ID, null))
                    .isInstanceOf(DishNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when new name already exists")
        void shouldThrowWhenNewNameExists() {
            Dish dish = createActiveDish();
            UpdateDishRequest request = updateDishRequest();
            request.setName("Existing Name");

            when(dishRepository.findByIdAndCookId(DISH_ID, COOK_ID)).thenReturn(Optional.of(dish));
            when(dishRepository.existsByCookIdAndNameAndIdNot(COOK_ID, "Existing Name", DISH_ID))
                    .thenReturn(true);

            assertThatThrownBy(() -> dishService.updateDish(DISH_ID, request, COOK_ID, null))
                    .isInstanceOf(BusinessValidationException.class);
        }
    }

    @Nested
    @DisplayName("getDishById")
    class GetDishByIdTests {

        @Test
        @DisplayName("Should return active dish")
        void shouldReturnActiveDish() {
            Dish dish = createActiveDish();

            when(dishRepository.findByIdAndIsActiveTrue(DISH_ID)).thenReturn(Optional.of(dish));
            when(dishMapper.toResponse(dish)).thenReturn(createDishResponse("Test Dish"));

            DishResponse result = dishService.getDishById(DISH_ID);

            assertThat(result.getId()).isEqualTo(DISH_ID);
        }

        @Test
        @DisplayName("Should throw exception when dish not found")
        void shouldThrowWhenNotFound() {
            when(dishRepository.findByIdAndIsActiveTrue(DISH_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> dishService.getDishById(DISH_ID))
                    .isInstanceOf(DishNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateStock")
    class UpdateStockTests {

        @Test
        @DisplayName("Should update stock and set available when stock > 0")
        void shouldUpdateStockAndSetAvailable() {
            Dish dish = createDish("Test", 0, false);
            UpdateStockRequest request = UpdateStockRequest.builder()
                    .currentStock(10)
                    .maxDailyStock(20)
                    .build();

            when(dishRepository.findByIdAndCookId(DISH_ID, COOK_ID)).thenReturn(Optional.of(dish));
            when(dishRepository.save(dish)).thenReturn(dish);
            when(dishMapper.toResponse(dish)).thenReturn(createDishResponse("Test"));

            dishService.updateStock(DISH_ID, request, COOK_ID);

            assertThat(dish.getCurrentStock()).isEqualTo(10);
            assertThat(dish.getAvailable()).isTrue();
            verify(eventPublisher).publishDishAvailabilityChanged(any());
        }

        @Test
        @DisplayName("Should set unavailable when stock is 0")
        void shouldSetUnavailableWhenStockZero() {
            Dish dish = createActiveDish();
            UpdateStockRequest request = UpdateStockRequest.builder()
                    .currentStock(0)
                    .build();

            when(dishRepository.findByIdAndCookId(DISH_ID, COOK_ID)).thenReturn(Optional.of(dish));
            when(dishRepository.save(dish)).thenReturn(dish);
            when(dishMapper.toResponse(dish)).thenReturn(createDishResponse("Test"));

            dishService.updateStock(DISH_ID, request, COOK_ID);

            assertThat(dish.getAvailable()).isFalse();
            verify(eventPublisher).publishDishAvailabilityChanged(any());
        }

        @Test
        @DisplayName("Should not publish event when availability unchanged")
        void shouldNotPublishEventWhenAvailabilityUnchanged() {
            Dish dish = createActiveDish();
            UpdateStockRequest request = UpdateStockRequest.builder()
                    .currentStock(10)
                    .build();

            when(dishRepository.findByIdAndCookId(DISH_ID, COOK_ID)).thenReturn(Optional.of(dish));
            when(dishRepository.save(dish)).thenReturn(dish);
            when(dishMapper.toResponse(dish)).thenReturn(createDishResponse("Test"));

            dishService.updateStock(DISH_ID, request, COOK_ID);

            verify(eventPublisher, never()).publishDishAvailabilityChanged(any());
        }
    }

    @Nested
    @DisplayName("decreaseStock")
    class DecreaseStockTests {

        @Test
        @DisplayName("Should decrease stock successfully")
        void shouldDecreaseStock() {
            Dish dish = createActiveDish();

            when(dishRepository.findById(DISH_ID)).thenReturn(Optional.of(dish));
            when(dishRepository.save(dish)).thenReturn(dish);

            dishService.decreaseStock(DISH_ID, 2);

            assertThat(dish.getCurrentStock()).isEqualTo(3);
            verify(dishRepository).save(dish);
        }

        @Test
        @DisplayName("Should publish event when becoming unavailable")
        void shouldPublishEventWhenBecomingUnavailable() {
            Dish dish = createActiveDish();
            dish.setCurrentStock(2);

            when(dishRepository.findById(DISH_ID)).thenReturn(Optional.of(dish));
            when(dishRepository.save(dish)).thenReturn(dish);

            dishService.decreaseStock(DISH_ID, 2);

            assertThat(dish.getAvailable()).isFalse();
            verify(eventPublisher).publishDishAvailabilityChanged(any());
        }

        @Test
        @DisplayName("Should throw exception when dish not available")
        void shouldThrowWhenDishNotAvailable() {
            Dish dish = createDish("Test", 5, false);

            when(dishRepository.findById(DISH_ID)).thenReturn(Optional.of(dish));

            assertThatThrownBy(() -> dishService.decreaseStock(DISH_ID, 2))
                    .isInstanceOf(DishNotAvailableException.class);
        }

        @Test
        @DisplayName("Should throw exception when insufficient stock")
        void shouldThrowWhenInsufficientStock() {
            Dish dish = createActiveDish();
            dish.setCurrentStock(3);

            when(dishRepository.findById(DISH_ID)).thenReturn(Optional.of(dish));

            assertThatThrownBy(() -> dishService.decreaseStock(DISH_ID, 5))
                    .isInstanceOf(InsufficientStockException.class);
        }
    }

    @Nested
    @DisplayName("increaseStock")
    class IncreaseStockTests {

        @Test
        @DisplayName("Should increase stock and set available")
        void shouldIncreaseStockAndSetAvailable() {
            Dish dish = createDish("Test", 0, false);

            when(dishRepository.findById(DISH_ID)).thenReturn(Optional.of(dish));
            when(dishRepository.save(dish)).thenReturn(dish);

            dishService.increaseStock(DISH_ID, 5);

            assertThat(dish.getCurrentStock()).isEqualTo(5);
            assertThat(dish.getAvailable()).isTrue();
        }

        @Test
        @DisplayName("Should not set available when dish inactive")
        void shouldNotSetAvailableWhenInactive() {
            Dish dish = createDish("Test", 0, false);
            dish.setIsActive(false);

            when(dishRepository.findById(DISH_ID)).thenReturn(Optional.of(dish));
            when(dishRepository.save(dish)).thenReturn(dish);

            dishService.increaseStock(DISH_ID, 5);

            assertThat(dish.getAvailable()).isFalse();
        }
    }

    @Nested
    @DisplayName("toggleAvailability")
    class ToggleAvailabilityTests {

        @Test
        @DisplayName("Should toggle availability from true to false")
        void shouldToggleFromTrueToFalse() {
            Dish dish = createActiveDish();

            when(dishRepository.findByIdAndCookId(DISH_ID, COOK_ID)).thenReturn(Optional.of(dish));
            when(dishRepository.save(dish)).thenReturn(dish);
            when(dishMapper.toResponse(dish)).thenReturn(createDishResponse("Test"));

            dishService.toggleAvailability(DISH_ID, COOK_ID);

            assertThat(dish.getAvailable()).isFalse();
            verify(eventPublisher).publishDishAvailabilityChanged(any());
        }

        @Test
        @DisplayName("Should toggle availability from false to true")
        void shouldToggleFromFalseToTrue() {
            Dish dish = createDish("Test", 5, false);

            when(dishRepository.findByIdAndCookId(DISH_ID, COOK_ID)).thenReturn(Optional.of(dish));
            when(dishRepository.save(dish)).thenReturn(dish);
            when(dishMapper.toResponse(dish)).thenReturn(createDishResponse("Test"));

            dishService.toggleAvailability(DISH_ID, COOK_ID);

            assertThat(dish.getAvailable()).isTrue();
            verify(eventPublisher).publishDishAvailabilityChanged(any());
        }
    }

    @Nested
    @DisplayName("deleteDish")
    class DeleteDishTests {

        @Test
        @DisplayName("Should soft delete dish and delete images")
        void shouldSoftDeleteDishAndImages() {
            Dish dish = createActiveDish();
            dish.setImages(List.of("image1.jpg", "image2.jpg"));

            when(dishRepository.findByIdAndCookId(DISH_ID, COOK_ID)).thenReturn(Optional.of(dish));
            when(dishRepository.save(dish)).thenReturn(dish);

            dishService.deleteDish(DISH_ID, COOK_ID);

            assertThat(dish.getIsActive()).isFalse();
            assertThat(dish.getAvailable()).isFalse();
            assertThat(dish.getDeactivationReason()).isNotNull();
            verify(fileStorageService).deleteFiles(any());
            verify(eventPublisher).publishDishDeleted(any());
        }

        @Test
        @DisplayName("Should soft delete dish without images")
        void shouldSoftDeleteDishWithoutImages() {
            Dish dish = createActiveDish();

            when(dishRepository.findByIdAndCookId(DISH_ID, COOK_ID)).thenReturn(Optional.of(dish));
            when(dishRepository.save(dish)).thenReturn(dish);

            dishService.deleteDish(DISH_ID, COOK_ID);

            verify(fileStorageService, never()).deleteFiles(any());
            verify(eventPublisher).publishDishDeleted(any());
        }
    }

    @Nested
    @DisplayName("updateOrderStats")
    class UpdateOrderStatsTests {

        @Test
        @DisplayName("Should update order statistics")
        void shouldUpdateOrderStats() {
            Dish dish = createActiveDish();
            BigDecimal orderAmount = new BigDecimal("50.00");

            when(dishRepository.findById(DISH_ID)).thenReturn(Optional.of(dish));
            when(dishRepository.save(dish)).thenReturn(dish);

            dishService.updateOrderStats(DISH_ID, orderAmount);

            assertThat(dish.getTotalOrders()).isEqualTo(1);
            assertThat(dish.getTotalRevenue()).isEqualByComparingTo(orderAmount);
        }
    }

    @Nested
    @DisplayName("updateDishRating")
    class UpdateDishRatingTests {

        @Test
        @DisplayName("Should update dish rating")
        void shouldUpdateRating() {
            Dish dish = createActiveDish();
            BigDecimal newRating = new BigDecimal("4.5");

            when(dishRepository.findById(DISH_ID)).thenReturn(Optional.of(dish));
            when(dishRepository.save(dish)).thenReturn(dish);

            dishService.updateDishRating(DISH_ID, newRating);

            assertThat(dish.getAverageRating()).isEqualByComparingTo(newRating);
            assertThat(dish.getTotalRatings()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getCookStatistics")
    class GetCookStatisticsTests {

        @Test
        @DisplayName("Should return cook statistics")
        void shouldReturnCookStats() {
            when(dishRepository.countTotalDishes(COOK_ID)).thenReturn(10L);
            when(dishRepository.countActiveDishesForCook(COOK_ID)).thenReturn(8L);
            when(dishRepository.countAvailableDishes(COOK_ID)).thenReturn(6L);
            when(dishRepository.getTotalRevenue(COOK_ID)).thenReturn(new BigDecimal("1000.00"));
            when(dishRepository.getTotalOrders(COOK_ID)).thenReturn(50L);
            when(dishRepository.getAverageRating(COOK_ID)).thenReturn(new BigDecimal("4.5"));
            when(dishRepository.getAveragePrice(COOK_ID)).thenReturn(new BigDecimal("25.00"));

            DishStatsResponse stats = dishService.getCookStatistics(COOK_ID);

            assertThat(stats.getTotalDishes()).isEqualTo(10L);
            assertThat(stats.getActiveDishes()).isEqualTo(8L);
            assertThat(stats.getAvailableDishes()).isEqualTo(6L);
            assertThat(stats.getOutOfStockDishes()).isEqualTo(4L);
            assertThat(stats.getTotalRevenue()).isEqualByComparingTo(new BigDecimal("1000.00"));
            assertThat(stats.getTotalOrders()).isEqualTo(50);
        }
    }
}