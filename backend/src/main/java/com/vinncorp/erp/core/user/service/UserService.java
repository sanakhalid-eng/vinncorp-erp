package com.vinncorp.erp.core.user.service;

import com.vinncorp.erp.core.auth.request.ChangePasswordRequest;
import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.user.request.RegisterRequest;
import com.vinncorp.erp.core.user.request.UpdateUserRequest;
import com.vinncorp.erp.core.user.response.UserResponse;
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

