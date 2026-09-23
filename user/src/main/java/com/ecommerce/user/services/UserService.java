package com.ecommerce.user.services;


import com.ecommerce.user.dto.AddressDTO;
import com.ecommerce.user.dto.UserRequest;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.models.Address;
import com.ecommerce.user.models.User;
import com.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final KeyCloakAdminService keyCloakAdminService;
//    private List<User> userList = new ArrayList<>();
//    private Long nextId = 1l;

    public List<UserResponse> fetchAllUsers(){
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public void addUser(UserRequest userRequest){
//        user.setId(nextId++);
        String token = keyCloakAdminService.getAdminAccessToken();
        String keycloakUserId = keyCloakAdminService.createUser(token , userRequest);

        User user = new User();
        updateUserFromRequest(user , userRequest);
        user.setKeycloakId(keycloakUserId);

        try {
            // Передаем токен первым аргументом (если обновили KeyCloakAdminService)
            keyCloakAdminService.assignClientRoleToUser(userRequest.getUserName(), "USER", keycloakUserId);
        } catch (Exception e) {
            // Если роль не назначилась, мы пишем ошибку в логи, но программа НЕ падает
            System.err.println("Предупреждение: Не удалось назначить роль в Keycloak: " + e.getMessage());
        }
        userRepository.save(user);
    }


    public Optional<UserResponse> fetchUser(String id) {
        return userRepository.findById(id)
                .map(this::mapToUserResponse);
    }

    public boolean updateUser(String id , UserRequest userRequest) {
       return userRepository.findById(id)
                .map(existingUser -> {
                    updateUserFromRequest(existingUser , userRequest);
                    userRepository.save(existingUser);
                    return true;
                })
                .orElse(false);
    }

    private void updateUserFromRequest(User user, UserRequest userRequest) {
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEmail(userRequest.getEmail());
        user.setPhone(userRequest.getPhone());

        if(userRequest.getAddress() != null){
            Address address = new Address();
            address.setCity(userRequest.getAddress().getCity());
            address.setState(userRequest.getAddress().getState());
            address.setCountry(userRequest.getAddress().getCountry());
            address.setStreet(userRequest.getAddress().getStreet());
            address.setZipcode(userRequest.getAddress().getZipcode());
            user.setAddress(address);
        }
    }

    private UserResponse mapToUserResponse(User user){
        UserResponse userResponse = new UserResponse();
        userResponse.setKeycloakId(user.getKeycloakId());
        userResponse.setId(String.valueOf(user.getId()));
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setEmail(user.getEmail());
        userResponse.setPhone(user.getPhone());
        userResponse.setRole(user.getRole());

        if(user.getAddress() != null){
            AddressDTO addressDTO = new AddressDTO();
            addressDTO.setCity(user.getAddress().getCity());
            addressDTO.setState(user.getAddress().getState());
            addressDTO.setCountry(user.getAddress().getCountry());
            addressDTO.setZipcode(user.getAddress().getZipcode());
            addressDTO.setStreet(user.getAddress().getStreet());
            userResponse.setAddress(addressDTO);
        }
        return userResponse;
    }
}
