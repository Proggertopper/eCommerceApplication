package com.ecommerce.user.services;

import com.ecommerce.user.dto.AddressDTO;
import com.ecommerce.user.dto.UserRequest;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.models.User;
import com.ecommerce.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KeyCloakAdminService keyCloakAdminService;

    @InjectMocks
    private UserService userService;

    @Test
    void fetchAllUsersMapsEntitiesToResponses() {
        when(userRepository.findAll()).thenReturn(List.of(user()));

        List<UserResponse> result = userService.fetchAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("user-1");
        assertThat(result.get(0).getEmail()).isEqualTo("sasha@example.com");
    }

    @Test
    void addUserCreatesKeycloakUserAssignsRoleAndPersistsUser() {
        UserRequest request = userRequest();
        when(keyCloakAdminService.getAdminAccessToken()).thenReturn("token");
        when(keyCloakAdminService.createUser("token", request)).thenReturn("keycloak-user-1");

        userService.addUser(request);

        verify(keyCloakAdminService).assignClientRoleToUser("sasha", "USER", "keycloak-user-1");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void fetchUserReturnsMappedResponseWhenFound() {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user()));

        Optional<UserResponse> result = userService.fetchUser("user-1");

        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("Sasha");
    }

    @Test
    void updateUserMutatesExistingUser() {
        User existing = user();
        UserRequest request = userRequest();
        request.setFirstName("Alex");
        when(userRepository.findById("user-1")).thenReturn(Optional.of(existing));

        boolean updated = userService.updateUser("user-1", request);

        assertThat(updated).isTrue();
        assertThat(existing.getFirstName()).isEqualTo("Alex");
        verify(userRepository).save(existing);
    }

    private User user() {
        User user = new User();
        user.setId("user-1");
        user.setKeycloakId("keycloak-user-1");
        user.setFirstName("Sasha");
        user.setLastName("Kosinskyi");
        user.setEmail("sasha@example.com");
        user.setPhone("+380000000000");
        return user;
    }

    private UserRequest userRequest() {
        AddressDTO address = new AddressDTO();
        address.setStreet("Main");
        address.setCity("Kyiv");
        address.setCountry("Ukraine");

        UserRequest request = new UserRequest();
        request.setUserName("sasha");
        request.setFirstName("Sasha");
        request.setLastName("Kosinskyi");
        request.setPassword("password");
        request.setEmail("sasha@example.com");
        request.setPhone("+380000000000");
        request.setAddress(address);
        return request;
    }
}
