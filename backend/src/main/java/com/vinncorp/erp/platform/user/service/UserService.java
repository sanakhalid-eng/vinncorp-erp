package com.vinncorp.erp.platform.user.service;

import com.vinncorp.erp.platform.auth.dto.request.ChangePasswordRequest;
import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.dto.request.RegisterRequest;
import com.vinncorp.erp.platform.user.dto.request.UpdateUserRequest;
import com.vinncorp.erp.platform.user.dto.response.UserResponse;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService {

    UserResponse updateUserByAdmin(Long id, UpdateUserRequest request);

    User getUserByEmail(String email);

    User getUserByEmailOrUsername(String identifier);

    List<User> getAllUsers();

    UserResponse updateMyProfile(String email, UpdateUserRequest request);

    void changePassword(String email, ChangePasswordRequest request);

    void deactivateMyAccount(String email);

    void deleteUser(Long id);

    User createUserFromRequest(@Valid RegisterRequest request);

    String uploadAvatar(String email, MultipartFile file) throws IOException;
}

