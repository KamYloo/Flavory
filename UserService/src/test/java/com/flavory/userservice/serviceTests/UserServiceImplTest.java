package com.flavory.userservice.serviceTests;

import com.flavory.userservice.dto.request.UpdateUserRequest;
import com.flavory.userservice.dto.response.UserResponse;
import com.flavory.userservice.entity.User;
import com.flavory.userservice.entity.enums.UserRole;
import com.flavory.userservice.entity.enums.UserStatus;
import com.flavory.userservice.exception.UserNotFoundException;
import com.flavory.userservice.mapper.UserMapper;
import com.flavory.userservice.messaging.publisher.UserEventPublisher;
import com.flavory.userservice.repository.UserRepository;
import com.flavory.userservice.security.JwtClaims;
import com.flavory.userservice.service.FileStorageService;
import com.flavory.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Tests")
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private UserEventPublisher eventPublisher;
    @Mock private FileStorageService fileStorageService;

    @InjectMocks
    private UserServiceImpl userService;

    private static final String AUTH0_ID = "auth0|123456";
    private static final Long USER_ID = 1L;

    private User createUser(UserStatus status, boolean verified) {
        return User.builder()
                .id(USER_ID)
                .auth0Id(UserServiceImplTest.AUTH0_ID)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.CUSTOMER)
                .status(status)
                .isVerified(verified)
                .build();
    }

    private User createActiveUser() {
        return createUser(UserStatus.ACTIVE, true);
    }

    private User createUnverifiedUser() {
        return createUser(UserStatus.PENDING_VERIFICATION, false);
    }

    private JwtClaims createClaims(String givenName, String familyName, Boolean emailVerified, List<String> roles) {
        return JwtClaims.builder()
                .sub(AUTH0_ID)
                .email("test@example.com")
                .givenName(givenName)
                .familyName(familyName)
                .picture("https://example.com/image.jpg")
                .emailVerified(emailVerified)
                .roles(roles)
                .build();
    }

    private JwtClaims createVerifiedClaims() {
        return createClaims("John", "Doe", true, List.of("Customer"));
    }

    private UserResponse createUserResponse() {
        return UserResponse.builder()
                .id(UserServiceImplTest.USER_ID)
                .auth0Id(AUTH0_ID)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .fullName("John Doe")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("getOrCreateUser")
    class GetOrCreateUserTests {

        @Test
        @DisplayName("Should return existing user without updates")
        void shouldReturnExistingUserWithoutUpdates() {
            User user = createActiveUser();
            JwtClaims claims = createVerifiedClaims();
            UserResponse response = createUserResponse();

            when(userRepository.findByAuth0Id(AUTH0_ID)).thenReturn(Optional.of(user));
            when(userMapper.toResponse(user)).thenReturn(response);

            UserResponse result = userService.getOrCreateUser(claims);

            assertThat(result.getId()).isEqualTo(USER_ID);
            verify(userRepository, never()).save(any());
            verify(eventPublisher, never()).publishUserUpdated(any());
        }

        @Test
        @DisplayName("Should update user when claims contain new data")
        void shouldUpdateUserWhenClaimsChange() {
            User user = createActiveUser();
            JwtClaims updatedClaims = createClaims("Jane", "Smith", true, null);

            when(userRepository.findByAuth0Id(AUTH0_ID)).thenReturn(Optional.of(user));
            when(userRepository.save(any())).thenReturn(user);
            when(userMapper.toResponse(any())).thenReturn(createUserResponse());

            userService.getOrCreateUser(updatedClaims);

            verify(userRepository).save(user);
            verify(eventPublisher).publishUserUpdated(user);
        }

        @Test
        @DisplayName("Should activate user when email becomes verified")
        void shouldActivateUserWhenEmailVerified() {
            User unverifiedUser = createUnverifiedUser();
            JwtClaims verifiedClaims = createVerifiedClaims();

            when(userRepository.findByAuth0Id(AUTH0_ID)).thenReturn(Optional.of(unverifiedUser));
            when(userRepository.save(any())).thenReturn(unverifiedUser);
            when(userMapper.toResponse(any())).thenReturn(createUserResponse());

            userService.getOrCreateUser(verifiedClaims);

            verify(userRepository).save(unverifiedUser);
            verify(eventPublisher).publishUserUpdated(unverifiedUser);
        }

        @Test
        @DisplayName("Should create new verified user")
        void shouldCreateNewVerifiedUser() {
            JwtClaims claims = createVerifiedClaims();
            User newUser = createActiveUser();

            when(userRepository.findByAuth0Id(AUTH0_ID)).thenReturn(Optional.empty());
            when(userRepository.save(any())).thenReturn(newUser);
            when(userMapper.toResponse(newUser)).thenReturn(createUserResponse());

            UserResponse result = userService.getOrCreateUser(claims);

            assertThat(result.getEmail()).isEqualTo("test@example.com");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());

            User savedUser = captor.getValue();
            assertThat(savedUser.getAuth0Id()).isEqualTo(AUTH0_ID);
            assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(savedUser.getIsVerified()).isTrue();

            verify(eventPublisher).publishUserUpdated(newUser);
        }

        @Test
        @DisplayName("Should create unverified user when email not verified")
        void shouldCreateUnverifiedUser() {
            JwtClaims unverifiedClaims = createClaims("John", "Doe", false, null);

            when(userRepository.findByAuth0Id(AUTH0_ID)).thenReturn(Optional.empty());
            when(userRepository.save(any())).thenReturn(createUnverifiedUser());
            when(userMapper.toResponse(any())).thenReturn(createUserResponse());

            userService.getOrCreateUser(unverifiedClaims);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());

            assertThat(captor.getValue().getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
            assertThat(captor.getValue().getIsVerified()).isFalse();
        }

        @Test
        @DisplayName("Should assign COOK role from claims")
        void shouldAssignCookRole() {
            JwtClaims cookClaims = createClaims("Chef", "Gordon", true, List.of("Cook"));

            when(userRepository.findByAuth0Id(AUTH0_ID)).thenReturn(Optional.empty());
            when(userRepository.save(any())).thenReturn(createActiveUser());
            when(userMapper.toResponse(any())).thenReturn(createUserResponse());

            userService.getOrCreateUser(cookClaims);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getRole()).isEqualTo(UserRole.COOK);
        }

        @Test
        @DisplayName("Should assign ADMIN role from claims")
        void shouldAssignAdminRole() {
            JwtClaims adminClaims = createClaims("Admin", "User", true, List.of("Admin"));

            when(userRepository.findByAuth0Id(AUTH0_ID)).thenReturn(Optional.empty());
            when(userRepository.save(any())).thenReturn(createActiveUser());
            when(userMapper.toResponse(any())).thenReturn(createUserResponse());

            userService.getOrCreateUser(adminClaims);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getRole()).isEqualTo(UserRole.ADMIN);
        }

        @Test
        @DisplayName("Should handle null names in claims")
        void shouldHandleNullNames() {
            JwtClaims claimsWithNullNames = createClaims(null, null, true, null);

            when(userRepository.findByAuth0Id(AUTH0_ID)).thenReturn(Optional.empty());
            when(userRepository.save(any())).thenReturn(createActiveUser());
            when(userMapper.toResponse(any())).thenReturn(createUserResponse());

            userService.getOrCreateUser(claimsWithNullNames);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());

            assertThat(captor.getValue().getFirstName()).isEqualTo("");
            assertThat(captor.getValue().getLastName()).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return user when exists")
        void shouldReturnUser() {
            User user = createActiveUser();
            UserResponse response = createUserResponse();

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(userMapper.toResponse(user)).thenReturn(response);

            UserResponse result = userService.getUserById(USER_ID);

            assertThat(result.getId()).isEqualTo(USER_ID);
            assertThat(result.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(USER_ID))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateUser")
    class UpdateUserTests {

        private UpdateUserRequest createUpdateRequest() {
            return UpdateUserRequest.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .phoneNumber("+48123456789")
                    .build();
        }

        @Test
        @DisplayName("Should update user without image")
        void shouldUpdateUserWithoutImage() {
            User user = createActiveUser();
            UpdateUserRequest request = createUpdateRequest();

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toResponse(user)).thenReturn(createUserResponse());

            UserResponse result = userService.updateUser(USER_ID, request, AUTH0_ID, null);

            assertThat(result).isNotNull();
            verify(userRepository).save(user);
            verify(eventPublisher).publishUserUpdated(user);
            verify(fileStorageService, never()).storeFile(any());
        }

        @Test
        @DisplayName("Should update user with new image")
        void shouldUpdateUserWithImage() {
            User user = createActiveUser();
            UpdateUserRequest request = createUpdateRequest();
            MultipartFile image = mock(MultipartFile.class);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(image.isEmpty()).thenReturn(false);
            when(fileStorageService.storeFile(image)).thenReturn("https://example.com/new-image.jpg");
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toResponse(user)).thenReturn(createUserResponse());

            userService.updateUser(USER_ID, request, AUTH0_ID, image);

            verify(fileStorageService).storeFile(image);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should not store empty image")
        void shouldNotStoreEmptyImage() {
            User user = createActiveUser();
            UpdateUserRequest request = createUpdateRequest();
            MultipartFile image = mock(MultipartFile.class);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(image.isEmpty()).thenReturn(true);
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toResponse(user)).thenReturn(createUserResponse());

            userService.updateUser(USER_ID, request, AUTH0_ID, image);

            verify(fileStorageService, never()).storeFile(any());
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user not found")
        void shouldThrowWhenUserNotFound() {
            UpdateUserRequest request = createUpdateRequest();

            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(USER_ID, request, AUTH0_ID, null))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw UserNotFoundException for wrong auth0Id")
        void shouldThrowWhenWrongAuth0Id() {
            User user = createActiveUser();
            UpdateUserRequest request = createUpdateRequest();

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> userService.updateUser(USER_ID, request, "wrong", null))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository, never()).save(any());
        }
    }
}