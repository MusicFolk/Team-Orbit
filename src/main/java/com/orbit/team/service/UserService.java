package com.orbit.team.service;

import com.orbit.team.dto.request.RegisterRequest;
import com.orbit.team.dto.response.UserResponse;
import com.orbit.team.entity.User;
import com.orbit.team.exception.DuplicateEmailException;
import com.orbit.team.exception.DuplicateUsernameException;
import com.orbit.team.exception.ResourceNotFoundException;
import com.orbit.team.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
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
        if(userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new DuplicateEmailException("This email is already in use");
        }
        if(userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new DuplicateUsernameException("This username is already in use");
        }
        User newUser = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .fullName(registerRequest.getFullName())
                .build();

        User saveUser = userRepository.save(newUser);

        return userResponse(saveUser);
    }

    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        return userResponse(user);
    }

    public List<UserResponse> getAllUsers(){
        return userRepository.findAll().stream().map(this::userResponse).toList();
    }

    public UserResponse setStatus(Long id, boolean status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        user.setActive(status);
        return userResponse(userRepository.save(user));
    }
}
