package com.vinncorp.erp.platform.user.service.impl;

import com.vinncorp.erp.platform.auth.dto.request.ChangePasswordRequest;
import com.vinncorp.erp.platform.user.dto.request.RegisterRequest;
import com.vinncorp.erp.platform.user.dto.request.UpdateUserRequest;
import com.vinncorp.erp.platform.user.dto.response.UserResponse;
import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.platform.user.service.UserService;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse updateUserByAdmin(Long id, UpdateUserRequest request) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    @Override
    public User getUserByEmailOrUsername(String identifier) {
        return userRepository.findByEmailOrUsername(identifier, identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + identifier));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public UserResponse updateMyProfile(String email, UpdateUserRequest request) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void changePassword(String email, ChangePasswordRequest request) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void deactivateMyAccount(String email) {
        User user = getUserByEmail(email);
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public User createUserFromRequest(RegisterRequest request) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String uploadAvatar(String email, MultipartFile file) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
