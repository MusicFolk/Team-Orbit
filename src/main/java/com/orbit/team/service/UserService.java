package com.orbit.team.service;

import com.orbit.team.dto.request.RegisterRequest;
import com.orbit.team.dto.response.UserResponse;
import com.orbit.team.entity.User;
import com.orbit.team.exception.DuplicateEmailException;
import com.orbit.team.exception.DuplicateUsernameException;
import com.orbit.team.exception.ResourceNotFoundException;
import com.orbit.team.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    private UserResponse userResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setActive(user.isActive());
        return response;
    }

    public UserResponse registerUser(RegisterRequest registerRequest) {
        log.info("Registering new user '{}'", registerRequest.getUsername());

        if(userRepository.existsByEmail(registerRequest.getEmail())) {
            log.warn("Registration failed. Email already exists.", registerRequest.getEmail());
            throw new DuplicateEmailException("This email is already in use");
        }
        if(userRepository.existsByUsername(registerRequest.getUsername())) {
            log.warn("Registration failed. Username already exists: {}", registerRequest.getUsername());
            throw new DuplicateUsernameException("This username is already in use");
        }
        User newUser = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .fullName(registerRequest.getFullName())
                .build();

        User saveUser = userRepository.save(newUser);
        log.info("User '{}' registered successfully with id {}", saveUser.getUsername(), saveUser.getId());

        return userResponse(saveUser);
    }

    public UserResponse getById(Long id) {
        log.info("Fetching user by id {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User {} not found", id);
                    return new ResourceNotFoundException("User not found: " + id);
                });
        return userResponse(user);
    }

    public List<UserResponse> getAllUsers(){
        log.info("Fetching all users");
        return userRepository.findAll().stream().map(this::userResponse).toList();
    }

    public UserResponse setStatus(Long id, boolean status) {
        log.info("Changing active status of user {} to {}", id, status);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cannot change status. User {} not found", id);
                    return new ResourceNotFoundException("User not found: " + id);
                });
        user.setActive(status);
        User updatedUser = userRepository.save(user);

        log.info("User {} status updated successfully", id);

        return userResponse(updatedUser);
    }
}
