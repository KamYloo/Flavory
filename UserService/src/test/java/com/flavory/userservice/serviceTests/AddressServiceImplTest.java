package com.flavory.userservice.serviceTests;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddressServiceImpl Tests")
class AddressServiceImplTest {

    @Mock private AddressRepository addressRepository;
    @Mock private UserRepository userRepository;
    @Mock private AddressMapper addressMapper;

    @InjectMocks
    private AddressServiceImpl addressService;

    private static final String AUTH0_ID = "auth0|123456";
    private static final Long USER_ID = 1L;
    private static final Long ADDRESS_ID = 1L;

    private User createUser() {
        return User.builder()
                .id(USER_ID)
                .auth0Id(AUTH0_ID)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.CUSTOMER)
                .addresses(new ArrayList<>())
                .build();
    }

    private User createUserWithAddresses(Address... addresses) {
        User user = createUser();
        user.getAddresses().addAll(List.of(addresses));
        return user;
    }

    private Address createAddress(Long id, boolean isDefault) {
        return Address.builder()
                .id(id)
                .street("Main Street")
                .city("Warsaw")
                .postalCode("00-001")
                .apartmentNumber("10")
                .country("Poland")
                .isDefault(isDefault)
                .label("Home")
                .latitude(52.2297)
                .longitude(21.0122)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Address createAddress() {
        return createAddress(ADDRESS_ID, true);
    }

    private CreateAddressRequest createAddressRequest(Boolean isDefault) {
        return CreateAddressRequest.builder()
                .street("Main Street")
                .city("Warsaw")
                .postalCode("00-001")
                .apartmentNumber("10")
                .country("Poland")
                .isDefault(isDefault)
                .label("Home")
                .latitude(52.2297)
                .longitude(21.0122)
                .build();
    }

    private UpdateAddressRequest updateAddressRequest() {
        return UpdateAddressRequest.builder()
                .street("New Street")
                .city("Krakow")
                .postalCode("30-001")
                .apartmentNumber("5")
                .label("Work")
                .build();
    }

    private AddressResponse createAddressResponse(Long id, boolean isDefault) {
        return AddressResponse.builder()
                .id(id)
                .street("Main Street")
                .city("Warsaw")
                .postalCode("00-001")
                .isDefault(isDefault)
                .build();
    }

    @Nested
    @DisplayName("createAddress")
    class CreateAddressTests {

        @Test
        @DisplayName("Should set first address as default automatically")
        void shouldSetFirstAddressAsDefault() {
            User user = createUser();
            Address address = createAddress();
            CreateAddressRequest request = createAddressRequest(false);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(addressMapper.toEntity(request)).thenReturn(address);
            when(addressRepository.save(any(Address.class))).thenReturn(address);
            when(addressMapper.toResponse(address)).thenReturn(createAddressResponse(ADDRESS_ID, true));

            AddressResponse result = addressService.createAddress(USER_ID, request, AUTH0_ID);

            assertThat(result.getIsDefault()).isTrue();
            verify(addressRepository, never()).clearDefaultAddress(USER_ID);
        }

        @Test
        @DisplayName("Should clear other defaults when creating default address")
        void shouldClearOtherDefaultsWhenCreatingDefault() {
            User user = createUserWithAddresses(createAddress());
            CreateAddressRequest request = createAddressRequest(true);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(addressMapper.toEntity(request)).thenReturn(createAddress());
            when(addressRepository.save(any())).thenReturn(createAddress());
            when(addressMapper.toResponse(any())).thenReturn(createAddressResponse(ADDRESS_ID, true));

            addressService.createAddress(USER_ID, request, AUTH0_ID);

            verify(addressRepository).clearDefaultAddress(USER_ID);
        }

        @Test
        @DisplayName("Should create non-default address when user has addresses")
        void shouldCreateNonDefaultWhenUserHasAddresses() {
            User user = createUserWithAddresses(createAddress());
            CreateAddressRequest request = createAddressRequest(false);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(addressMapper.toEntity(request)).thenReturn(createAddress(2L, false));
            when(addressRepository.save(any())).thenReturn(createAddress(2L, false));
            when(addressMapper.toResponse(any())).thenReturn(createAddressResponse(2L, false));

            AddressResponse result = addressService.createAddress(USER_ID, request, AUTH0_ID);

            assertThat(result.getIsDefault()).isFalse();
            verify(addressRepository, never()).clearDefaultAddress(USER_ID);
        }

        @Test
        @DisplayName("Should set default country when null")
        void shouldSetDefaultCountryWhenNull() {
            CreateAddressRequest request = createAddressRequest(false);
            request.setCountry(null);

            Address address = createAddress();
            address.setCountry(null);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(createUser()));
            when(addressMapper.toEntity(request)).thenReturn(address);
            when(addressRepository.save(any())).thenReturn(address);
            when(addressMapper.toResponse(any())).thenReturn(createAddressResponse(ADDRESS_ID, true));

            addressService.createAddress(USER_ID, request, AUTH0_ID);

            ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
            verify(addressRepository).save(captor.capture());
            assertThat(captor.getValue().getCountry()).isEqualTo("Poland");
        }

        @Test
        @DisplayName("Should set default country when empty")
        void shouldSetDefaultCountryWhenEmpty() {
            CreateAddressRequest request = createAddressRequest(false);
            request.setCountry("");

            Address address = createAddress();
            address.setCountry("");

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(createUser()));
            when(addressMapper.toEntity(request)).thenReturn(address);
            when(addressRepository.save(any())).thenReturn(address);
            when(addressMapper.toResponse(any())).thenReturn(createAddressResponse(ADDRESS_ID, true));

            addressService.createAddress(USER_ID, request, AUTH0_ID);

            ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
            verify(addressRepository).save(captor.capture());
            assertThat(captor.getValue().getCountry()).isEqualTo("Poland");
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user not found")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> addressService.createAddress(USER_ID, createAddressRequest(false), AUTH0_ID))
                    .isInstanceOf(UserNotFoundException.class);

            verify(addressRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw UnauthorizedAccessException for wrong auth0Id")
        void shouldThrowWhenUnauthorized() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(createUser()));

            assertThatThrownBy(() -> addressService.createAddress(USER_ID, createAddressRequest(false), "wrong"))
                    .isInstanceOf(UnauthorizedAccessException.class);

            verify(addressRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getAddressById")
    class GetAddressByIdTests {

        @Test
        @DisplayName("Should return address when exists")
        void shouldReturnAddress() {
            Address address = createAddress();
            AddressResponse response = createAddressResponse(ADDRESS_ID, true);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(createUser()));
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.of(address));
            when(addressMapper.toResponse(address)).thenReturn(response);

            AddressResponse result = addressService.getAddressById(USER_ID, ADDRESS_ID, AUTH0_ID);

            assertThat(result.getId()).isEqualTo(ADDRESS_ID);
        }

        @Test
        @DisplayName("Should throw AddressNotFoundException when not found")
        void shouldThrowWhenAddressNotFound() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(createUser()));
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> addressService.getAddressById(USER_ID, ADDRESS_ID, AUTH0_ID))
                    .isInstanceOf(AddressNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw UnauthorizedAccessException for wrong auth0Id")
        void shouldThrowWhenUnauthorized() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(createUser()));

            assertThatThrownBy(() -> addressService.getAddressById(USER_ID, ADDRESS_ID, "wrong"))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }
    }

    @Nested
    @DisplayName("getUserAddresses")
    class GetUserAddressesTests {

        @Test
        @DisplayName("Should return all user addresses")
        void shouldReturnAllAddresses() {
            List<Address> addresses = List.of(createAddress(1L, true), createAddress(2L, false));

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(createUser()));
            when(addressRepository.findByUserId(USER_ID)).thenReturn(addresses);
            when(addressMapper.toResponse(any())).thenAnswer(inv -> {
                Address addr = inv.getArgument(0);
                return createAddressResponse(addr.getId(), addr.getIsDefault());
            });

            List<AddressResponse> result = addressService.getUserAddresses(USER_ID, AUTH0_ID);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(1).getId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should return empty list when no addresses")
        void shouldReturnEmptyList() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(createUser()));
            when(addressRepository.findByUserId(USER_ID)).thenReturn(List.of());

            List<AddressResponse> result = addressService.getUserAddresses(USER_ID, AUTH0_ID);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should throw UnauthorizedAccessException for wrong auth0Id")
        void shouldThrowWhenUnauthorized() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(createUser()));

            assertThatThrownBy(() -> addressService.getUserAddresses(USER_ID, "wrong"))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }
    }

    @Nested
    @DisplayName("getDefaultAddressByAuth0Id")
    class GetDefaultAddressByAuth0IdTests {

        @Test
        @DisplayName("Should return default address")
        void shouldReturnDefaultAddress() {
            Address address = createAddress();
            AddressResponse response = createAddressResponse(ADDRESS_ID, true);

            when(userRepository.findByAuth0Id(AUTH0_ID)).thenReturn(Optional.of(createUser()));
            when(addressRepository.findDefaultAddressByAuth0Id(AUTH0_ID)).thenReturn(Optional.of(address));
            when(addressMapper.toResponse(address)).thenReturn(response);

            AddressResponse result = addressService.getDefaultAddressByAuth0Id(AUTH0_ID);

            assertThat(result.getIsDefault()).isTrue();
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user not found")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByAuth0Id(AUTH0_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> addressService.getDefaultAddressByAuth0Id(AUTH0_ID))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw AddressNotFoundException when no default")
        void shouldThrowWhenNoDefault() {
            when(userRepository.findByAuth0Id(AUTH0_ID)).thenReturn(Optional.of(createUser()));
            when(addressRepository.findDefaultAddressByAuth0Id(AUTH0_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> addressService.getDefaultAddressByAuth0Id(AUTH0_ID))
                    .isInstanceOf(AddressNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateAddress")
    class UpdateAddressTests {

        @Test
        @DisplayName("Should update non-default address")
        void shouldUpdateNonDefaultAddress() {
            Address address = createAddress(ADDRESS_ID, false);
            UpdateAddressRequest request = updateAddressRequest();

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(createUser()));
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.of(address));
            when(addressRepository.save(address)).thenReturn(address);
            when(addressMapper.toResponse(address)).thenReturn(createAddressResponse(ADDRESS_ID, false));

            AddressResponse result = addressService.updateAddress(USER_ID, ADDRESS_ID, request, AUTH0_ID);

            assertThat(result).isNotNull();
            verify(addressRepository).save(address);
            verify(addressRepository, never()).clearDefaultAddress(USER_ID);
        }

        @Test
        @DisplayName("Should clear defaults when updating to default")
        void shouldClearDefaultsWhenUpdatingToDefault() {
            Address address = createAddress(ADDRESS_ID, true);
            UpdateAddressRequest request = updateAddressRequest();

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(createUser()));
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.of(address));
            when(addressRepository.save(address)).thenReturn(address);
            when(addressMapper.toResponse(address)).thenReturn(createAddressResponse(ADDRESS_ID, true));

            addressService.updateAddress(USER_ID, ADDRESS_ID, request, AUTH0_ID);

            verify(addressRepository).clearDefaultAddress(USER_ID);
            assertThat(address.getIsDefault()).isTrue();
        }

        @Test
        @DisplayName("Should throw AddressNotFoundException when not found")
        void shouldThrowWhenAddressNotFound() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(createUser()));
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> addressService.updateAddress(USER_ID, ADDRESS_ID, updateAddressRequest(), AUTH0_ID))
                    .isInstanceOf(AddressNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw UnauthorizedAccessException for wrong auth0Id")
        void shouldThrowWhenUnauthorized() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(createUser()));

            assertThatThrownBy(() -> addressService.updateAddress(USER_ID, ADDRESS_ID, updateAddressRequest(), "wrong"))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }
    }

    @Nested
    @DisplayName("setDefaultAddress")
    class SetDefaultAddressTests {

        @Test
        @DisplayName("Should set address as default")
        void shouldSetAsDefault() {
            Address address = createAddress(ADDRESS_ID, false);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(createUser()));
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.of(address));
            when(addressRepository.save(address)).thenReturn(address);
            when(addressMapper.toResponse(address)).thenReturn(createAddressResponse(ADDRESS_ID, true));

            addressService.setDefaultAddress(USER_ID, ADDRESS_ID, AUTH0_ID);

            verify(addressRepository).clearDefaultAddress(USER_ID);
            assertThat(address.getIsDefault()).isTrue();
        }

        @Test
        @DisplayName("Should throw AddressNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(createUser()));
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> addressService.setDefaultAddress(USER_ID, ADDRESS_ID, AUTH0_ID))
                    .isInstanceOf(AddressNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw UnauthorizedAccessException for wrong auth0Id")
        void shouldThrowWhenUnauthorized() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(createUser()));

            assertThatThrownBy(() -> addressService.setDefaultAddress(USER_ID, ADDRESS_ID, "wrong"))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }
    }

    @Nested
    @DisplayName("deleteAddress")
    class DeleteAddressTests {

        @Test
        @DisplayName("Should delete non-default address")
        void shouldDeleteNonDefault() {
            Address address = createAddress(ADDRESS_ID, false);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(createUser()));
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.of(address));

            addressService.deleteAddress(USER_ID, ADDRESS_ID, AUTH0_ID);

            verify(addressRepository).delete(address);
            verify(addressRepository, never()).findByUserId(USER_ID);
        }

        @Test
        @DisplayName("Should set new default when deleting current default")
        void shouldSetNewDefaultWhenDeletingDefault() {
            Address defaultAddress = createAddress(ADDRESS_ID, true);
            Address otherAddress = createAddress(2L, false);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(createUser()));
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.of(defaultAddress));
            when(addressRepository.findByUserId(USER_ID)).thenReturn(List.of(otherAddress));

            addressService.deleteAddress(USER_ID, ADDRESS_ID, AUTH0_ID);

            verify(addressRepository).delete(defaultAddress);
            verify(addressRepository).save(otherAddress);
            assertThat(otherAddress.getIsDefault()).isTrue();
        }

        @Test
        @DisplayName("Should not set default when deleting last address")
        void shouldNotSetDefaultWhenDeletingLast() {
            Address address = createAddress(ADDRESS_ID, true);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(createUser()));
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.of(address));
            when(addressRepository.findByUserId(USER_ID)).thenReturn(List.of());

            addressService.deleteAddress(USER_ID, ADDRESS_ID, AUTH0_ID);

            verify(addressRepository).delete(address);
            verify(addressRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw AddressNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(createUser()));
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> addressService.deleteAddress(USER_ID, ADDRESS_ID, AUTH0_ID))
                    .isInstanceOf(AddressNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw UnauthorizedAccessException for wrong auth0Id")
        void shouldThrowWhenUnauthorized() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(createUser()));

            assertThatThrownBy(() -> addressService.deleteAddress(USER_ID, ADDRESS_ID, "wrong"))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }
    }
}