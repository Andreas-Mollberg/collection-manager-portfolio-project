package com.example.collection_manager.services;

import com.example.collection_manager.dtos.UserDTOMapper;
import com.example.collection_manager.dtos.UserDTO;
import com.example.collection_manager.models.User;
import com.example.collection_manager.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDTOMapper userDTOMapper;

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public Optional<UserDTO> findUserById(Long id) {
        return userRepository.findById(id)
                .map(userDTOMapper::toDTO);
    }

    public List<UserDTO> findAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userDTOMapper::toDTO)
                .collect(Collectors.toList());
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

}
