package com.parksync.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parksync.backend.dto.request.LoginRequest;
import com.parksync.backend.dto.request.RegisterRequest;
import com.parksync.backend.dto.request.UpdateProfileRequest;
import com.parksync.backend.dto.response.AuthResponse;
import com.parksync.backend.dto.response.UserResponse;
import com.parksync.backend.entity.AppUser;
import com.parksync.backend.exception.BadRequestException;
import com.parksync.backend.exception.ConflictException;
import com.parksync.backend.exception.ResourceNotFoundException;
import com.parksync.backend.exception.UnauthorizedException;
import com.parksync.backend.repository.AppUserRepository;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AppUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (appUserRepository.existsByEmail(email)) {
            throw new ConflictException("Email is already registered: " + email);
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            if (appUserRepository.existsByPhone(request.getPhone().trim())) {
                throw new ConflictException("Phone number is already registered: " + request.getPhone());
            }
        }

        AppUser user = new AppUser();
        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        
        String role = request.getRole() != null && request.getRole().equalsIgnoreCase("ROLE_ADMIN") 
                ? "ROLE_ADMIN" 
                : "ROLE_USER";
        user.setRole(role);
        user.setIsActive(true);

        AppUser savedUser = appUserRepository.save(user);
        String token = jwtService.generateToken(savedUser);

        return new AuthResponse(token, UserResponse.fromEntity(savedUser));
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new UnauthorizedException("Account is deactivated. Please contact administrator.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, UserResponse.fromEntity(user));
    }

    public AppUser getRequiredUserById(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    public UserResponse getUserProfile(Long userId) {
        return UserResponse.fromEntity(getRequiredUserById(userId));
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        AppUser user = getRequiredUserById(userId);

        user.setFullName(request.getFullName().trim());
        if (request.getPhone() != null) {
            String newPhone = request.getPhone().trim();
            if (!newPhone.equals(user.getPhone()) && appUserRepository.existsByPhone(newPhone)) {
                throw new ConflictException("Phone number is already registered to another account");
            }
            user.setPhone(newPhone.isBlank() ? null : newPhone);
        }

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (request.getNewPassword().length() < 6) {
                throw new BadRequestException("New password must be at least 6 characters");
            }
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }

        AppUser saved = appUserRepository.save(user);
        return UserResponse.fromEntity(saved);
    }

    public List<UserResponse> getAllUsers() {
        return appUserRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse toggleUserActive(Long userId, boolean isActive) {
        AppUser user = getRequiredUserById(userId);
        user.setIsActive(isActive);
        return UserResponse.fromEntity(appUserRepository.save(user));
    }
}
