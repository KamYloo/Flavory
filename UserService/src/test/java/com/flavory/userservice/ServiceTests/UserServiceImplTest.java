package com.flavory.userservice.ServiceTests;

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
import org.junit.jupiter.api.BeforeEach;
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

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserEventPublisher eventPublisher;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserResponse testUserResponse;
    private JwtClaims testClaims;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .auth0Id("auth0|123456")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .profileImageUrl("https://example.com/image.jpg")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .isVerified(true)
                .build();

        testUserResponse = UserResponse.builder()
                .id(1L)
                .auth0Id("auth0|123456")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .fullName("John Doe")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        testClaims = JwtClaims.builder()
                .sub("auth0|123456")
                .email("test@example.com")
                .givenName("John")
                .familyName("Doe")
                .picture("https://example.com/image.jpg")
                .emailVerified(true)
                .roles(List.of("Customer"))
                .build();
    }

    @Nested
    @DisplayName("getOrCreateUser Tests")
    class GetOrCreateUserTests {

        @Test
        @DisplayName("Should return existing user when user exists and no updates needed")
        void shouldReturnExistingUserWhenNoUpdatesNeeded() {
            when(userRepository.findByAuth0Id(testClaims.getSub())).thenReturn(Optional.of(testUser));
            when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

            UserResponse result = userService.getOrCreateUser(testClaims);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getEmail()).isEqualTo("test@example.com");

            verify(userRepository).findByAuth0Id(testClaims.getSub());
            verify(userRepository, never()).save(any());
            verify(eventPublisher, never()).publishUserUpdated(any());
            verify(userMapper).toResponse(testUser);
        }

        @Test
        @DisplayName("Should update and save user when claims contain new data")
        void shouldUpdateUserWhenClaimsContainNewData() {
            JwtClaims updatedClaims = JwtClaims.builder()
                    .sub("auth0|123456")
                    .email("test@example.com")
                    .givenName("Jane")
                    .familyName("Smith")
                    .emailVerified(true)
                    .build();

            User updatedUser = User.builder()
                    .id(1L)
                    .auth0Id("auth0|123456")
                    .email("test@example.com")
                    .firstName("Jane")
                    .lastName("Smith")
                    .role(UserRole.CUSTOMER)
                    .status(UserStatus.ACTIVE)
                    .isVerified(true)
                    .build();

            when(userRepository.findByAuth0Id(updatedClaims.getSub())).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(updatedUser);
            when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

            UserResponse result = userService.getOrCreateUser(updatedClaims);

            assertThat(result).isNotNull();

            verify(userRepository).findByAuth0Id(updatedClaims.getSub());
            verify(userRepository).save(any(User.class));
            verify(eventPublisher).publishUserUpdated(any(User.class));
        }

        @Test
        @DisplayName("Should create new user when user does not exist")
        void shouldCreateNewUserWhenUserDoesNotExist() {
            when(userRepository.findByAuth0Id(testClaims.getSub())).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

            UserResponse result = userService.getOrCreateUser(testClaims);

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("test@example.com");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getAuth0Id()).isEqualTo(testClaims.getSub());
            assertThat(savedUser.getEmail()).isEqualTo(testClaims.getEmail());
            assertThat(savedUser.getFirstName()).isEqualTo(testClaims.getGivenName());
            assertThat(savedUser.getLastName()).isEqualTo(testClaims.getFamilyName());
            assertThat(savedUser.getRole()).isEqualTo(UserRole.CUSTOMER);
            assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);

            verify(eventPublisher).publishUserUpdated(testUser);
        }

        @Test
        @DisplayName("Should create user with PENDING_VERIFICATION status when email not verified")
        void shouldCreateUserWithPendingStatusWhenEmailNotVerified() {
            JwtClaims unverifiedClaims = JwtClaims.builder()
                    .sub("auth0|123456")
                    .email("test@example.com")
                    .givenName("John")
                    .familyName("Doe")
                    .emailVerified(false)
                    .build();

            when(userRepository.findByAuth0Id(unverifiedClaims.getSub())).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

            userService.getOrCreateUser(unverifiedClaims);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
            assertThat(savedUser.getIsVerified()).isFalse();
        }

        @Test
        @DisplayName("Should create user with COOK role when role is in claims")
        void shouldCreateUserWithCookRoleWhenSpecified() {
            JwtClaims cookClaims = JwtClaims.builder()
                    .sub("auth0|123456")
                    .email("cook@example.com")
                    .givenName("Chef")
                    .familyName("Gordon")
                    .emailVerified(true)
                    .roles(List.of("Cook"))
                    .build();

            when(userRepository.findByAuth0Id(cookClaims.getSub())).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

            userService.getOrCreateUser(cookClaims);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getRole()).isEqualTo(UserRole.COOK);
        }

        @Test
        @DisplayName("Should create user with ADMIN role when admin role is in claims")
        void shouldCreateUserWithAdminRoleWhenSpecified() {
            JwtClaims adminClaims = JwtClaims.builder()
                    .sub("auth0|123456")
                    .email("admin@example.com")
                    .givenName("Admin")
                    .familyName("User")
                    .emailVerified(true)
                    .roles(List.of("Admin"))
                    .build();

            when(userRepository.findByAuth0Id(adminClaims.getSub())).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

            userService.getOrCreateUser(adminClaims);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getRole()).isEqualTo(UserRole.ADMIN);
        }

        @Test
        @DisplayName("Should handle null givenName and familyName in claims")
        void shouldHandleNullNamesInClaims() {
            JwtClaims claimsWithNullNames = JwtClaims.builder()
                    .sub("auth0|123456")
                    .email("test@example.com")
                    .givenName(null)
                    .familyName(null)
                    .emailVerified(true)
                    .build();

            when(userRepository.findByAuth0Id(claimsWithNullNames.getSub())).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

            userService.getOrCreateUser(claimsWithNullNames);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getFirstName()).isEqualTo("");
            assertThat(savedUser.getLastName()).isEqualTo("");
        }

        @Test
        @DisplayName("Should update user to ACTIVE when email becomes verified")
        void shouldUpdateUserToActiveWhenEmailVerified() {
            User unverifiedUser = User.builder()
                    .id(1L)
                    .auth0Id("auth0|123456")
                    .email("test@example.com")
                    .firstName("John")
                    .lastName("Doe")
                    .role(UserRole.CUSTOMER)
                    .status(UserStatus.PENDING_VERIFICATION)
                    .isVerified(false)
                    .build();

            JwtClaims verifiedClaims = JwtClaims.builder()
                    .sub("auth0|123456")
                    .email("test@example.com")
                    .givenName("John")
                    .familyName("Doe")
                    .emailVerified(true)
                    .build();

            when(userRepository.findByAuth0Id(verifiedClaims.getSub())).thenReturn(Optional.of(unverifiedUser));
            when(userRepository.save(any(User.class))).thenReturn(unverifiedUser);
            when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

            userService.getOrCreateUser(verifiedClaims);

            verify(userRepository).save(any(User.class));
            verify(eventPublisher).publishUserUpdated(any(User.class));
        }
    }

    @Nested
    @DisplayName("getUserById Tests")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return user when user exists")
        void shouldReturnUserWhenExists() {
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

            UserResponse result = userService.getUserById(userId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(userId);
            assertThat(result.getEmail()).isEqualTo("test@example.com");

            verify(userRepository).findById(userId);
            verify(userMapper).toResponse(testUser);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user does not exist")
        void shouldThrowExceptionWhenUserNotFound() {
            Long userId = 999L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(userId))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository).findById(userId);
            verify(userMapper, never()).toResponse(any());
        }
    }

    @Nested
    @DisplayName("updateUser Tests")
    class UpdateUserTests {

        private UpdateUserRequest updateRequest;
        private MultipartFile mockImage;

        @BeforeEach
        void setUp() {
            updateRequest = UpdateUserRequest.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .phoneNumber("+48123456789")
                    .build();

            mockImage = mock(MultipartFile.class);
        }

        @Test
        @DisplayName("Should update user successfully without image")
        void shouldUpdateUserWithoutImage() {
            Long userId = 1L;
            String currentAuth0Id = "auth0|123456";

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

            UserResponse result = userService.updateUser(userId, updateRequest, currentAuth0Id, null);

            assertThat(result).isNotNull();

            verify(userRepository).findById(userId);
            verify(userMapper).updateEntityFromDto(updateRequest, testUser);
            verify(userRepository).save(testUser);
            verify(eventPublisher).publishUserUpdated(testUser);
            verify(fileStorageService, never()).storeFile(any());
        }

        @Test
        @DisplayName("Should update user with new profile image")
        void shouldUpdateUserWithImage() {
            Long userId = 1L;
            String currentAuth0Id = "auth0|123456";
            String newImageUrl = "https://example.com/new-image.jpg";

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(mockImage.isEmpty()).thenReturn(false);
            when(fileStorageService.storeFile(mockImage)).thenReturn(newImageUrl);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

            UserResponse result = userService.updateUser(userId, updateRequest, currentAuth0Id, mockImage);

            assertThat(result).isNotNull();

            verify(userRepository).findById(userId);
            verify(fileStorageService).storeFile(mockImage);
            verify(userRepository).save(testUser);
            verify(eventPublisher).publishUserUpdated(testUser);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
        }

        @Test
        @DisplayName("Should not store image when image is empty")
        void shouldNotStoreEmptyImage() {
            Long userId = 1L;
            String currentAuth0Id = "auth0|123456";

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(mockImage.isEmpty()).thenReturn(true);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

            userService.updateUser(userId, updateRequest, currentAuth0Id, mockImage);

            verify(fileStorageService, never()).storeFile(any());
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user does not exist")
        void shouldThrowExceptionWhenUserNotFound() {
            Long userId = 999L;
            String currentAuth0Id = "auth0|123456";

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(userId, updateRequest, currentAuth0Id, null))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository).findById(userId);
            verify(userMapper, never()).updateEntityFromDto(any(), any());
            verify(userRepository, never()).save(any());
            verify(eventPublisher, never()).publishUserUpdated(any());
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when auth0Id does not match")
        void shouldThrowExceptionWhenAuth0IdDoesNotMatch() {
            Long userId = 1L;
            String wrongAuth0Id = "auth0|different";

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            assertThatThrownBy(() -> userService.updateUser(userId, updateRequest, wrongAuth0Id, null))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository).findById(userId);
            verify(userMapper, never()).updateEntityFromDto(any(), any());
            verify(userRepository, never()).save(any());
            verify(eventPublisher, never()).publishUserUpdated(any());
        }
    }
}