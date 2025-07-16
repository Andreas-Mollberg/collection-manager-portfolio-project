package com.example.collection_manager.controllers;

import com.example.collection_manager.dtos.UserDTO;
import com.example.collection_manager.dtos.UserDTOMapper;
import com.example.collection_manager.models.User;
import com.example.collection_manager.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserDTOMapper userDTOMapper;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userService.emailExists(user.getEmail())) {
            return ResponseEntity
                    .status(409)
                    .body("Email is already registered.");
        }
        User savedUser = userService.saveUser(user);
        return ResponseEntity.ok(userDTOMapper.toDTO(savedUser));
    }


    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        return userService.findUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.findAllUsers();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        userService.deleteUser(id);
    }
}
