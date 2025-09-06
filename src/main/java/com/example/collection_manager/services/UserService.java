package com.example.collection_manager.services;

import com.example.collection_manager.dtos.UserDTO;
import com.example.collection_manager.dtos.UserDTOMapper;
import com.example.collection_manager.models.User;
import com.example.collection_manager.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserDTOMapper userDTOMapper;

    public UserService(UserRepository userRepository, UserDTOMapper userDTOMapper) {
        this.userRepository = userRepository;
        this.userDTOMapper = userDTOMapper;
    }

    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<UserDTO> findUserById(Long id) {
        return userRepository.findById(id)
                .map(userDTOMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> findAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userDTOMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUserName(username);
    }

    @Transactional
    public void updateUser(User user) {
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Transactional(readOnly = true)
    public boolean usernameExists(String username) {
        return userRepository.findByUserName(username).isPresent();
    }
}
