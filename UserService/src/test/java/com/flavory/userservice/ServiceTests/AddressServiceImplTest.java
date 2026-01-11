package com.flavory.userservice.ServiceTests;

import com.flavory.userservice.dto.request.CreateAddressRequest;
import com.flavory.userservice.dto.request.UpdateAddressRequest;
import com.flavory.userservice.dto.response.AddressResponse;
import com.flavory.userservice.entity.Address;
import com.flavory.userservice.entity.User;
import com.flavory.userservice.entity.enums.UserRole;
import com.flavory.userservice.exception.AddressNotFoundException;
import com.flavory.userservice.exception.UnauthorizedAccessException;
import com.flavory.userservice.exception.UserNotFoundException;
import com.flavory.userservice.mapper.AddressMapper;
import com.flavory.userservice.repository.AddressRepository;
import com.flavory.userservice.repository.UserRepository;
import com.flavory.userservice.service.impl.AddressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddressServiceImpl Tests")
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressServiceImpl addressService;

    private User testUser;
    private Address testAddress;
    private AddressResponse testAddressResponse;
    private CreateAddressRequest createAddressRequest;
    private UpdateAddressRequest updateAddressRequest;

    private static final String AUTH0_ID = "auth0|123456";
    private static final Long USER_ID = 1L;
    private static final Long ADDRESS_ID = 1L;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(USER_ID)
                .auth0Id(AUTH0_ID)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.CUSTOMER)
                .addresses(new ArrayList<>())
                .build();

        testAddress = Address.builder()
                .id(ADDRESS_ID)
                .user(testUser)
                .street("Main Street")
                .city("Warsaw")
                .postalCode("00-001")
                .apartmentNumber("10")
                .country("Poland")
                .isDefault(true)
                .label("Home")
                .latitude(52.2297)
                .longitude(21.0122)
                .createdAt(LocalDateTime.now())
                .build();

        testAddressResponse = AddressResponse.builder()
                .id(ADDRESS_ID)
                .street("Main Street")
                .city("Warsaw")
                .postalCode("00-001")
                .apartmentNumber("10")
                .country("Poland")
                .fullAddress("Main Street, 00-001 Warsaw, Poland")
                .isDefault(true)
                .label("Home")
                .latitude(52.2297)
                .longitude(21.0122)
                .createdAt(LocalDateTime.now())
                .build();

        createAddressRequest = CreateAddressRequest.builder()
                .street("Main Street")
                .city("Warsaw")
                .postalCode("00-001")
                .apartmentNumber("10")
                .country("Poland")
                .isDefault(false)
                .label("Home")
                .latitude(52.2297)
                .longitude(21.0122)
                .build();

        updateAddressRequest = UpdateAddressRequest.builder()
                .street("New Street")
                .city("Krakow")
                .postalCode("30-001")
                .apartmentNumber("5")
                .country("Poland")
                .label("Work")
                .latitude(50.0647)
                .longitude(19.9450)
                .build();
    }

    @Nested
    @DisplayName("createAddress Tests")
    class CreateAddressTests {

        @Test
        @DisplayName("Should create address successfully for user with no existing addresses")
        void shouldCreateFirstAddressAsDefault() {
            // Given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(addressMapper.toEntity(createAddressRequest)).thenReturn(testAddress);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(addressMapper.toResponse(testAddress)).thenReturn(testAddressResponse);

            // When
            AddressResponse result = addressService.createAddress(USER_ID, createAddressRequest, AUTH0_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(ADDRESS_ID);
            assertThat(result.getStreet()).isEqualTo("Main Street");
            assertThat(result.getCity()).isEqualTo("Warsaw");
            assertThat(result.getPostalCode()).isEqualTo("00-001");
            assertThat(result.getApartmentNumber()).isEqualTo("10");
            assertThat(result.getLabel()).isEqualTo("Home");
            assertThat(result.getLatitude()).isEqualTo(52.2297);
            assertThat(result.getLongitude()).isEqualTo(21.0122);

            ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);
            verify(addressRepository).save(addressCaptor.capture());

            Address savedAddress = addressCaptor.getValue();
            assertThat(savedAddress.getIsDefault()).isTrue();
            assertThat(savedAddress.getUser()).isEqualTo(testUser);

            verify(addressRepository, never()).clearDefaultAddress(USER_ID);
        }

        @Test
        @DisplayName("Should create address as default when explicitly requested")
        void shouldCreateAddressAsDefaultWhenRequested() {
            // Given
            testUser.getAddresses().add(testAddress);
            CreateAddressRequest defaultRequest = CreateAddressRequest.builder()
                    .street("Main Street")
                    .city("Warsaw")
                    .postalCode("00-001")
                    .apartmentNumber("10")
                    .isDefault(true)
                    .label("Home")
                    .build();

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(addressMapper.toEntity(defaultRequest)).thenReturn(testAddress);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(addressMapper.toResponse(testAddress)).thenReturn(testAddressResponse);

            // When
            AddressResponse result = addressService.createAddress(USER_ID, defaultRequest, AUTH0_ID);

            // Then
            assertThat(result).isNotNull();

            verify(addressRepository).clearDefaultAddress(USER_ID);

            ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);
            verify(addressRepository).save(addressCaptor.capture());
            assertThat(addressCaptor.getValue().getIsDefault()).isTrue();
        }

        @Test
        @DisplayName("Should create address as non-default when user has existing addresses")
        void shouldCreateAddressAsNonDefault() {
            // Given
            testUser.getAddresses().add(testAddress);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(addressMapper.toEntity(createAddressRequest)).thenReturn(testAddress);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(addressMapper.toResponse(testAddress)).thenReturn(testAddressResponse);

            // When
            AddressResponse result = addressService.createAddress(USER_ID, createAddressRequest, AUTH0_ID);

            // Then
            assertThat(result).isNotNull();

            verify(addressRepository, never()).clearDefaultAddress(USER_ID);

            ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);
            verify(addressRepository).save(addressCaptor.capture());
            assertThat(addressCaptor.getValue().getIsDefault()).isFalse();
        }

        @Test
        @DisplayName("Should set default country to Poland when country is null")
        void shouldSetDefaultCountryWhenNull() {
            // Given
            CreateAddressRequest requestWithoutCountry = CreateAddressRequest.builder()
                    .street("Main Street")
                    .city("Warsaw")
                    .postalCode("00-001")
                    .country(null)
                    .build();

            Address addressWithoutCountry = Address.builder()
                    .street("Main Street")
                    .city("Warsaw")
                    .postalCode("00-001")
                    .build();

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(addressMapper.toEntity(requestWithoutCountry)).thenReturn(addressWithoutCountry);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(addressMapper.toResponse(any(Address.class))).thenReturn(testAddressResponse);

            // When
            addressService.createAddress(USER_ID, requestWithoutCountry, AUTH0_ID);

            // Then
            ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);
            verify(addressRepository).save(addressCaptor.capture());
            assertThat(addressCaptor.getValue().getCountry()).isEqualTo("Poland");
        }

        @Test
        @DisplayName("Should set default country to Poland when country is empty")
        void shouldSetDefaultCountryWhenEmpty() {
            // Given
            CreateAddressRequest requestWithEmptyCountry = CreateAddressRequest.builder()
                    .street("Main Street")
                    .city("Warsaw")
                    .postalCode("00-001")
                    .country("")
                    .build();

            Address addressWithEmptyCountry = Address.builder()
                    .street("Main Street")
                    .city("Warsaw")
                    .postalCode("00-001")
                    .country("")
                    .build();

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(addressMapper.toEntity(requestWithEmptyCountry)).thenReturn(addressWithEmptyCountry);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(addressMapper.toResponse(any(Address.class))).thenReturn(testAddressResponse);

            // When
            addressService.createAddress(USER_ID, requestWithEmptyCountry, AUTH0_ID);

            // Then
            ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);
            verify(addressRepository).save(addressCaptor.capture());
            assertThat(addressCaptor.getValue().getCountry()).isEqualTo("Poland");
        }

        @Test
        @DisplayName("Should create address with all optional fields")
        void shouldCreateAddressWithOptionalFields() {
            // Given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(addressMapper.toEntity(createAddressRequest)).thenReturn(testAddress);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(addressMapper.toResponse(testAddress)).thenReturn(testAddressResponse);

            // When
            AddressResponse result = addressService.createAddress(USER_ID, createAddressRequest, AUTH0_ID);

            // Then
            assertThat(result.getApartmentNumber()).isEqualTo("10");
            assertThat(result.getLabel()).isEqualTo("Home");
            assertThat(result.getLatitude()).isNotNull();
            assertThat(result.getLongitude()).isNotNull();
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user does not exist")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> addressService.createAddress(USER_ID, createAddressRequest, AUTH0_ID))
                    .isInstanceOf(UserNotFoundException.class);

            verify(addressRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw UnauthorizedAccessException when auth0Id does not match")
        void shouldThrowExceptionWhenUnauthorized() {
            // Given
            String wrongAuth0Id = "auth0|different";
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> addressService.createAddress(USER_ID, createAddressRequest, wrongAuth0Id))
                    .isInstanceOf(UnauthorizedAccessException.class);

            verify(addressRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getAddressById Tests")
    class GetAddressByIdTests {

        @Test
        @DisplayName("Should return address when address exists")
        void shouldReturnAddressWhenExists() {
            // Given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID))
                    .thenReturn(Optional.of(testAddress));
            when(addressMapper.toResponse(testAddress)).thenReturn(testAddressResponse);

            // When
            AddressResponse result = addressService.getAddressById(USER_ID, ADDRESS_ID, AUTH0_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(ADDRESS_ID);
            assertThat(result.getStreet()).isEqualTo("Main Street");
            assertThat(result.getFullAddress()).isEqualTo("Main Street, 00-001 Warsaw, Poland");
            assertThat(result.getCreatedAt()).isNotNull();

            verify(addressRepository).findByIdAndUserId(ADDRESS_ID, USER_ID);
        }

        @Test
        @DisplayName("Should throw AddressNotFoundException when address does not exist")
        void shouldThrowExceptionWhenAddressNotFound() {
            // Given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> addressService.getAddressById(USER_ID, ADDRESS_ID, AUTH0_ID))
                    .isInstanceOf(AddressNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw UnauthorizedAccessException when auth0Id does not match")
        void shouldThrowExceptionWhenUnauthorized() {
            // Given
            String wrongAuth0Id = "auth0|different";
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> addressService.getAddressById(USER_ID, ADDRESS_ID, wrongAuth0Id))
                    .isInstanceOf(UnauthorizedAccessException.class);

            verify(addressRepository, never()).findByIdAndUserId(any(), any());
        }
    }

    @Nested
    @DisplayName("getUserAddresses Tests")
    class GetUserAddressesTests {

        @Test
        @DisplayName("Should return all user addresses")
        void shouldReturnAllUserAddresses() {
            // Given
            Address address2 = Address.builder()
                    .id(2L)
                    .user(testUser)
                    .street("Second Street")
                    .city("Krakow")
                    .postalCode("30-001")
                    .apartmentNumber("5")
                    .country("Poland")
                    .isDefault(false)
                    .label("Work")
                    .latitude(50.0647)
                    .longitude(19.9450)
                    .createdAt(LocalDateTime.now())
                    .build();

            AddressResponse response2 = AddressResponse.builder()
                    .id(2L)
                    .street("Second Street")
                    .city("Krakow")
                    .postalCode("30-001")
                    .apartmentNumber("5")
                    .country("Poland")
                    .fullAddress("Second Street, 30-001 Krakow, Poland")
                    .isDefault(false)
                    .label("Work")
                    .latitude(50.0647)
                    .longitude(19.9450)
                    .createdAt(LocalDateTime.now())
                    .build();

            List<Address> addresses = Arrays.asList(testAddress, address2);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(addressRepository.findByUserId(USER_ID)).thenReturn(addresses);
            when(addressMapper.toResponse(testAddress)).thenReturn(testAddressResponse);
            when(addressMapper.toResponse(address2)).thenReturn(response2);

            // When
            List<AddressResponse> result = addressService.getUserAddresses(USER_ID, AUTH0_ID);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(ADDRESS_ID);
            assertThat(result.get(0).getLabel()).isEqualTo("Home");
            assertThat(result.get(1).getId()).isEqualTo(2L);
            assertThat(result.get(1).getLabel()).isEqualTo("Work");

            verify(addressRepository).findByUserId(USER_ID);
            verify(addressMapper, times(2)).toResponse(any(Address.class));
        }

        @Test
        @DisplayName("Should return empty list when user has no addresses")
        void shouldReturnEmptyListWhenNoAddresses() {
            // Given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(addressRepository.findByUserId(USER_ID)).thenReturn(new ArrayList<>());

            // When
            List<AddressResponse> result = addressService.getUserAddresses(USER_ID, AUTH0_ID);

            // Then
            assertThat(result).isEmpty();
            verify(addressRepository).findByUserId(USER_ID);
        }

        @Test
        @DisplayName("Should throw UnauthorizedAccessException when auth0Id does not match")
        void shouldThrowExceptionWhenUnauthorized() {
            // Given
            String wrongAuth0Id = "auth0|different";
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> addressService.getUserAddresses(USER_ID, wrongAuth0Id))
                    .isInstanceOf(UnauthorizedAccessException.class);

            verify(addressRepository, never()).findByUserId(any());
        }
    }

    @Nested
    @DisplayName("getDefaultAddressByAuth0Id Tests")
    class GetDefaultAddressByAuth0IdTests {

        @Test
        @DisplayName("Should return default address for authenticated user")
        void shouldReturnDefaultAddress() {
            // Given
            when(userRepository.findByAuth0Id(AUTH0_ID)).thenReturn(Optional.of(testUser));
            when(addressRepository.findDefaultAddressByAuth0Id(AUTH0_ID))
                    .thenReturn(Optional.of(testAddress));
            when(addressMapper.toResponse(testAddress)).thenReturn(testAddressResponse);

            // When
            AddressResponse result = addressService.getDefaultAddressByAuth0Id(AUTH0_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getIsDefault()).isTrue();

            verify(userRepository).findByAuth0Id(AUTH0_ID);
            verify(addressRepository).findDefaultAddressByAuth0Id(AUTH0_ID);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user does not exist")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userRepository.findByAuth0Id(AUTH0_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> addressService.getDefaultAddressByAuth0Id(AUTH0_ID))
                    .isInstanceOf(UserNotFoundException.class);

            verify(addressRepository, never()).findDefaultAddressByAuth0Id(any());
        }

        @Test
        @DisplayName("Should throw AddressNotFoundException when no default address exists")
        void shouldThrowExceptionWhenNoDefaultAddress() {
            // Given
            when(userRepository.findByAuth0Id(AUTH0_ID)).thenReturn(Optional.of(testUser));
            when(addressRepository.findDefaultAddressByAuth0Id(AUTH0_ID))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> addressService.getDefaultAddressByAuth0Id(AUTH0_ID))
                    .isInstanceOf(AddressNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateAddress Tests")
    class UpdateAddressTests {

        @Test
        @DisplayName("Should update address successfully")
        void shouldUpdateAddressSuccessfully() {
            // Given
            testAddress.setIsDefault(false);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID))
                    .thenReturn(Optional.of(testAddress));
            when(addressRepository.save(testAddress)).thenReturn(testAddress);
            when(addressMapper.toResponse(testAddress)).thenReturn(testAddressResponse);

            // When
            AddressResponse result = addressService.updateAddress(USER_ID, ADDRESS_ID, updateAddressRequest, AUTH0_ID);

            // Then
            assertThat(result).isNotNull();

            verify(addressMapper).updateEntityFromDto(updateAddressRequest, testAddress);
            verify(addressRepository).save(testAddress);
            verify(addressRepository, never()).clearDefaultAddress(USER_ID);
        }

        @Test
        @DisplayName("Should clear other default addresses when updating to default")
        void shouldClearOtherDefaultAddressesWhenUpdatingToDefault() {
            // Given
            testAddress.setIsDefault(true);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID))
                    .thenReturn(Optional.of(testAddress));
            when(addressRepository.save(testAddress)).thenReturn(testAddress);
            when(addressMapper.toResponse(testAddress)).thenReturn(testAddressResponse);

            // When
            AddressResponse result = addressService.updateAddress(USER_ID, ADDRESS_ID, updateAddressRequest, AUTH0_ID);

            // Then
            assertThat(result).isNotNull();

            verify(addressRepository).clearDefaultAddress(USER_ID);
            verify(addressRepository).save(testAddress);
            assertThat(testAddress.getIsDefault()).isTrue();
        }

        @Test
        @DisplayName("Should update all address fields including optional ones")
        void shouldUpdateAllAddressFields() {
            // Given
            testAddress.setIsDefault(false);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID))
                    .thenReturn(Optional.of(testAddress));
            when(addressRepository.save(testAddress)).thenReturn(testAddress);

            AddressResponse updatedResponse = AddressResponse.builder()
                    .id(ADDRESS_ID)
                    .street("New Street")
                    .city("Krakow")
                    .postalCode("30-001")
                    .apartmentNumber("5")
                    .country("Poland")
                    .label("Work")
                    .latitude(50.0647)
                    .longitude(19.9450)
                    .build();

            when(addressMapper.toResponse(testAddress)).thenReturn(updatedResponse);

            // When
            AddressResponse result = addressService.updateAddress(USER_ID, ADDRESS_ID, updateAddressRequest, AUTH0_ID);

            // Then
            assertThat(result.getStreet()).isEqualTo("New Street");
            assertThat(result.getCity()).isEqualTo("Krakow");
            assertThat(result.getPostalCode()).isEqualTo("30-001");
            assertThat(result.getApartmentNumber()).isEqualTo("5");
            assertThat(result.getLabel()).isEqualTo("Work");
            assertThat(result.getLatitude()).isEqualTo(50.0647);
            assertThat(result.getLongitude()).isEqualTo(19.9450);
        }

        @Test
        @DisplayName("Should throw AddressNotFoundException when address does not exist")
        void shouldThrowExceptionWhenAddressNotFound() {
            // Given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> addressService.updateAddress(USER_ID, ADDRESS_ID, updateAddressRequest, AUTH0_ID))
                    .isInstanceOf(AddressNotFoundException.class);

            verify(addressRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw UnauthorizedAccessException when auth0Id does not match")
        void shouldThrowExceptionWhenUnauthorized() {
            // Given
            String wrongAuth0Id = "auth0|different";
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> addressService.updateAddress(USER_ID, ADDRESS_ID, updateAddressRequest, wrongAuth0Id))
                    .isInstanceOf(UnauthorizedAccessException.class);

            verify(addressRepository, never()).findByIdAndUserId(any(), any());
            verify(addressRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("setDefaultAddress Tests")
    class SetDefaultAddressTests {

        @Test
        @DisplayName("Should set address as default successfully")
        void shouldSetAddressAsDefault() {
            // Given
            testAddress.setIsDefault(false);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID))
                    .thenReturn(Optional.of(testAddress));
            when(addressRepository.save(testAddress)).thenReturn(testAddress);
            when(addressMapper.toResponse(testAddress)).thenReturn(testAddressResponse);

            // When
            AddressResponse result = addressService.setDefaultAddress(USER_ID, ADDRESS_ID, AUTH0_ID);

            // Then
            assertThat(result).isNotNull();

            verify(addressRepository).clearDefaultAddress(USER_ID);

            ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);
            verify(addressRepository).save(addressCaptor.capture());
            assertThat(addressCaptor.getValue().getIsDefault()).isTrue();
        }

        @Test
        @DisplayName("Should throw AddressNotFoundException when address does not exist")
        void shouldThrowExceptionWhenAddressNotFound() {
            // Given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> addressService.setDefaultAddress(USER_ID, ADDRESS_ID, AUTH0_ID))
                    .isInstanceOf(AddressNotFoundException.class);

            verify(addressRepository, never()).clearDefaultAddress(any());
            verify(addressRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw UnauthorizedAccessException when auth0Id does not match")
        void shouldThrowExceptionWhenUnauthorized() {
            // Given
            String wrongAuth0Id = "auth0|different";
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> addressService.setDefaultAddress(USER_ID, ADDRESS_ID, wrongAuth0Id))
                    .isInstanceOf(UnauthorizedAccessException.class);

            verify(addressRepository, never()).findByIdAndUserId(any(), any());
        }
    }

    @Nested
    @DisplayName("deleteAddress Tests")
    class DeleteAddressTests {

        @Test
        @DisplayName("Should delete non-default address successfully")
        void shouldDeleteNonDefaultAddress() {
            // Given
            testAddress.setIsDefault(false);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID))
                    .thenReturn(Optional.of(testAddress));

            // When
            addressService.deleteAddress(USER_ID, ADDRESS_ID, AUTH0_ID);

            // Then
            verify(addressRepository).delete(testAddress);
            verify(addressRepository, never()).findByUserId(USER_ID);
        }

        @Test
        @DisplayName("Should set new default when deleting default address with remaining addresses")
        void shouldSetNewDefaultWhenDeletingDefaultAddress() {
            // Given
            testAddress.setIsDefault(true);

            Address address2 = Address.builder()
                    .id(2L)
                    .user(testUser)
                    .street("Second Street")
                    .city("Krakow")
                    .postalCode("30-001")
                    .isDefault(false)
                    .build();

            List<Address> remainingAddresses = Collections.singletonList(address2);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID))
                    .thenReturn(Optional.of(testAddress));
            when(addressRepository.findByUserId(USER_ID)).thenReturn(remainingAddresses);

            // When
            addressService.deleteAddress(USER_ID, ADDRESS_ID, AUTH0_ID);

            // Then
            verify(addressRepository).delete(testAddress);
            verify(addressRepository).findByUserId(USER_ID);

            ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);
            verify(addressRepository).save(addressCaptor.capture());
            assertThat(addressCaptor.getValue().getIsDefault()).isTrue();
            assertThat(addressCaptor.getValue().getId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should not set new default when deleting last address")
        void shouldNotSetDefaultWhenDeletingLastAddress() {
            // Given
            testAddress.setIsDefault(true);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID))
                    .thenReturn(Optional.of(testAddress));
            when(addressRepository.findByUserId(USER_ID)).thenReturn(new ArrayList<>());

            // When
            addressService.deleteAddress(USER_ID, ADDRESS_ID, AUTH0_ID);

            // Then
            verify(addressRepository).delete(testAddress);
            verify(addressRepository).findByUserId(USER_ID);
            verify(addressRepository, times(0)).save(any());
        }

        @Test
        @DisplayName("Should throw AddressNotFoundException when address does not exist")
        void shouldThrowExceptionWhenAddressNotFound() {
            // Given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> addressService.deleteAddress(USER_ID, ADDRESS_ID, AUTH0_ID))
                    .isInstanceOf(AddressNotFoundException.class);

            verify(addressRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw UnauthorizedAccessException when auth0Id does not match")
        void shouldThrowExceptionWhenUnauthorized() {
            // Given
            String wrongAuth0Id = "auth0|different";
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> addressService.deleteAddress(USER_ID, ADDRESS_ID, wrongAuth0Id))
                    .isInstanceOf(UnauthorizedAccessException.class);

            verify(addressRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("getUserAndValidateAccess Tests")
    class GetUserAndValidateAccessTests {

        @Test
        @DisplayName("Should return user when validation passes")
        void shouldReturnUserWhenValid() {
            // Given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

            // When - call through public method that uses private method
            when(addressRepository.findByUserId(USER_ID)).thenReturn(new ArrayList<>());
            addressService.getUserAddresses(USER_ID, AUTH0_ID);

            // Then
            verify(userRepository).findById(USER_ID);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user does not exist")
        void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
            // Given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> addressService.getUserAddresses(USER_ID, AUTH0_ID))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw UnauthorizedAccessException when auth0Id mismatch")
        void shouldThrowUnauthorizedExceptionWhenAuth0IdMismatch() {
            // Given
            String wrongAuth0Id = "auth0|wrong";
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> addressService.getUserAddresses(USER_ID, wrongAuth0Id))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }
    }
}