package com.ecommerce.user.controllers;

import com.ecommerce.user.dto.UserRequest;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.services.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;


    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(){
//        System.out.println("Request received");
        return ResponseEntity.ok(userService.fetchAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String id){
        log.info("Request received for user: {}" , id);
//        User user = userService.fetchUser(id);
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.add("Authorization" , "Bearer my-code");
//        httpHeaders.add("X-Custom-Header", "MyBackendValue");
//        if(user == null){
//            return ResponseEntity.notFound().headers(httpHeaders).build();
//        }
//        return ResponseEntity.ok(user);
        return userService.fetchUser(id)
                .map(ResponseEntity::ok)
                .orElseGet(()-> ResponseEntity.notFound().build());

    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable String id, @Valid @RequestBody UserRequest userRequest){
        boolean updatedUser = userService.updateUser(id , userRequest);
        if(updatedUser){
            return ResponseEntity.ok("User updated successfully");
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<String> createUser(@Valid @RequestBody UserRequest userRequest){
       userService.addUser(userRequest);
       return ResponseEntity.ok("User added successfully");
    }

//    @DeleteMapping("/api/users/{id}")
//    public List<User> deleteUser(@PathVariable("id") int userId){
//        userList.remove(userId);
//        return userList;
//    }
}
