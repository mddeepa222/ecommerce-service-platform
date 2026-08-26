package com.cartflow.authservice.service;

import com.cartflow.authservice.dto.UpdateProfileRequest;
import com.cartflow.authservice.dto.UserResponse;
import com.cartflow.authservice.entity.Role;
import com.cartflow.authservice.entity.User;
import com.cartflow.authservice.repository.RefreshTokenRepository;
import com.cartflow.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    // ─── Admin Operations ───────────────────────────────────────────────

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    public List<UserResponse> searchUsers(String query) {
        return userRepository.searchUsers(query)
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        User user = findById(id);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse changeRole(Long id, Role newRole) {
        User user = findById(id);
        user.setRole(newRole);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = findById(id);
        refreshTokenRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    // ─── User Self-Service Operations ───────────────────────────────────

    public UserResponse getMyProfile(String email) {
        User user = findByEmail(email);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateMyProfile(String email, UpdateProfileRequest request) {
        User user = findByEmail(email);

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse uploadAvatar(String email, MultipartFile file) {
        User user = findByEmail(email);

        if (user.getAvatarPublicId() != null) {
            cloudinaryService.deleteImage(user.getAvatarPublicId());
        }

        Map<String, String> uploaded = cloudinaryService.uploadImage(file);
        user.setAvatarUrl(uploaded.get("url"));
        user.setAvatarPublicId(uploaded.get("publicId"));

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void deleteAvatar(String email) {
        User user = findByEmail(email);
        if (user.getAvatarPublicId() == null) {
            throw new RuntimeException("No avatar to delete");
        }
        cloudinaryService.deleteImage(user.getAvatarPublicId());
        user.setAvatarUrl(null);
        user.setAvatarPublicId(null);
        userRepository.save(user);
    }

    @Transactional
    public void deleteMyAccount(String email) {
        User user = findByEmail(email);
        refreshTokenRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    // ─── Helpers ────────────────────────────────────────────────────────

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}
