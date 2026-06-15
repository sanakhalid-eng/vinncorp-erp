package com.vinncorp.erp.core.user.service.Impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.vinncorp.erp.core.auth.request.ChangePasswordRequest;
import com.vinncorp.erp.core.user.service.UserService;
import com.vinncorp.erp.core.user.constants.PermissionConstants;
import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.user.entity.UserRole;
import com.vinncorp.erp.core.user.repository.RoleRepository;
import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.core.user.request.RegisterRequest;
import com.vinncorp.erp.core.user.request.UpdateUserRequest;
import com.vinncorp.erp.core.user.response.UserResponse;
import com.vinncorp.erp.modules.projects.entity.Role;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.EmailAlreadyExistsException;
import com.vinncorp.erp.shared.exception.FileUploadException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;

import static com.vinncorp.erp.core.user.mapper.UserMapper.toResponse;

import java.awt.image.BufferedImage;
import java.io.IOException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final Cloudinary cloudinary;

    @Override
    @Transactional
    public UserResponse updateUserByAdmin(Long id, UpdateUserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Update name
        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }

        // Update email with duplicate check
        String newEmail = request.getEmail().trim().toLowerCase();

        if (!user.getEmail().equals(newEmail) &&
                userRepository.existsByEmail(newEmail)) {
            throw new EmailAlreadyExistsException("Email already in use");
        }

        user.setEmail(newEmail);

        // Update role (IMPORTANT)
        if (request.getRole() != null) {

            Role role = roleRepository.findByName(request.getRole())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

            // remove old roles
            user.getUserRoles().clear();

            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(role);
            userRole.setAssignedAt(LocalDateTime.now());

            user.getUserRoles().add(userRole);
        }

        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public User createUserFromRequest(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        String email = request.getEmail().trim().toLowerCase();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // User will be assigned role upon email verification
        user.setUserRoles(new HashSet<>());

        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findByIsActiveTrue();
    }

//    @Override
//    public UserResponse getUserById(Long id) {
//
//        User user = userRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
//
//        return toResponse(user);
//    }

//    @Override
//    public UserResponse updateUser(Long id, RegisterRequest request) {
//        // 1️- Fetch user or throw exception
//        User user = userRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
//
//        // 2️- Update fields (only if not null)
//        if (request.getName() != null) {
//            user.setName(request.getName());
//        }
//
//        if (request.getEmail() != null) {
//            user.setEmail(request.getEmail());
//        }
//
//        if (request.getRole() != null) {
//            user.setRole(Role.valueOf(request.getRole()));
//        }
//
//        // 3️- Save updated user
//        User updatedUser = userRepository.save(user);
//
//        // 4️- Return DTO response
//        return toResponse(updatedUser);
//    }

    @Override
    public void deleteUser(Long id) {

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    public User getUserByEmailOrUsername(String identifier) {
        return userRepository.findByEmailOrUsername(identifier, identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + identifier));
    }

    @Override
    public UserResponse updateMyProfile(String email, UpdateUserRequest request) {

        // 1️- Get logged-in user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2️- Update name (if provided)
        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }

        // 3️- Update email (if provided)
        if (request.getEmail() != null && !request.getEmail().isBlank()) {

            // Prevent duplicate email
            String newEmail = request.getEmail().trim().toLowerCase();

            if (!user.getEmail().equals(newEmail) &&
                    userRepository.existsByEmail(newEmail)) {
                throw new EmailAlreadyExistsException("Email already in use");
            }

            user.setEmail(newEmail);
        }

        if (request.getRole() != null && !request.getRole().isBlank()) {
            String requestedRole = request.getRole().trim().toUpperCase();

            if (!Set.of(PermissionConstants.PROJECT_MANAGER, PermissionConstants.TEAM_MEMBER).contains(requestedRole)) {
                throw new BadRequestException("Only project manager or team member can be selected");
            }

            Role role = roleRepository.findByName(requestedRole)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

            boolean alreadyAssigned = user.getUserRoles().stream()
                    .anyMatch(ur -> ur.getRole().getId().equals(role.getId()));

            if (!alreadyAssigned) {
                user.getUserRoles().clear();

                UserRole userRole = new UserRole();
                userRole.setUser(user);
                userRole.setRole(role);
                userRole.setAssignedAt(LocalDateTime.now());

                user.getUserRoles().add(userRole);
            }
        }

        // 4️- Save updated user
        User updatedUser = userRepository.save(user);

        // 5️- Return DTO
        return toResponse(updatedUser);
    }

    @Override
    public void changePassword(String email, ChangePasswordRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 1️- Validate inputs
        if (request.getNewPassword() == null || request.getConfirmPassword() == null) {
            throw new BadRequestException("Password fields cannot be null");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        // 2️- Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }

        // 3️- Prevent reuse
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password cannot be same as old password");
        }

        // 4️- enforce strength
        if (request.getNewPassword().length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters");
        }

        // 5️- Save
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void deactivateMyAccount(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setActive(false);

        // clear roles
        user.getUserRoles().clear();

        userRepository.save(user);
    }

    @Override
    public String uploadAvatar(String email, MultipartFile file) throws IOException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validation
        BufferedImage image = ImageIO.read(file.getInputStream());

        if (image == null) {
            throw new FileUploadException("Invalid image file");
        }

        if (file.isEmpty()) {
            throw new FileUploadException("File is empty");
        }

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new FileUploadException("Only image files are allowed");
        }

        if (file.getSize() > 2 * 1024 * 1024) {
            throw new FileUploadException("File size must be less than 2MB");
        }

        try {
            var uploadResult = cloudinary.uploader().upload(
                    file.getBytes(), // better than InputStream for validation safety
                    ObjectUtils.asMap(
                            "folder", "avatars",
                            "public_id", "users/" + user.getId(),
                            "overwrite", true,
                            "invalidate", true
                    )
            );

            String url = (String) uploadResult.get("secure_url");

            user.setAvatarUrl(url);
            userRepository.save(user);

            return url;

        } catch (IOException e) {
            throw new FileUploadException("Failed to upload avatar");
        }
    }

}


