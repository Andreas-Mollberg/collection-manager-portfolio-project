package com.example.collection_manager.controllers;

import com.example.collection_manager.dtos.RegisterUserDTO;
import com.example.collection_manager.dtos.UserDTO;
import com.example.collection_manager.dtos.UserDTOMapper;
import com.example.collection_manager.models.User;
import com.example.collection_manager.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserDTOMapper userDTOMapper;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService,
                          UserDTOMapper userDTOMapper,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userDTOMapper = userDTOMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterUserDTO dto) {

        if (userService.emailExists(dto.email())) {
            return ResponseEntity.status(409).body("Email is already registered.");
        }
        if (userService.usernameExists(dto.userName())) {
            return ResponseEntity.status(409).body("Username is already taken.");
        }

        User newUser = new User();
        newUser.setUserName(dto.userName());
        newUser.setEmail(dto.email());
        newUser.setPassword(passwordEncoder.encode(dto.password()));

        User savedUser = userService.saveUser(newUser);

        URI location = URI.create("/api/users/" + savedUser.getId());
        return ResponseEntity.created(location).body(userDTOMapper.toDTO(savedUser));
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
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
