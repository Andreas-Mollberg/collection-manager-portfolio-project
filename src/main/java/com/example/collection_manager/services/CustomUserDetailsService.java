package com.example.collection_manager.services;

import com.example.collection_manager.models.User;
import com.example.collection_manager.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.Objects;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String raw) throws UsernameNotFoundException {
        String username = raw == null ? null : raw.trim();
        if (username == null || username.isEmpty()) {
            throw new UsernameNotFoundException("Username is empty");
        }

        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new org.springframework.security.core.userdetails.User(
                Objects.requireNonNullElse(user.getUserName(), ""),
                Objects.requireNonNullElse(user.getPassword(), ""),
                Collections.emptyList()
        );
    }
}
