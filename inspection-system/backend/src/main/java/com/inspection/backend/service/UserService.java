package com.inspection.backend.service;

import com.inspection.backend.entity.User;
import com.inspection.backend.entity.Role;
import com.inspection.backend.entity.Department;
import com.inspection.backend.dto.UserDto;
import com.inspection.backend.dto.UserCreationRequestDto;
import com.inspection.backend.dto.UserUpdateRequestDto;
import com.inspection.backend.mapper.UserMapper;
import com.inspection.backend.repository.UserRepository;
import com.inspection.backend.repository.RoleRepository;
import com.inspection.backend.repository.DepartmentRepository;
import org.springframework.security.crypto.password.PasswordEncoder; // Assuming Spring Security for password encoding
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder; // Using Spring Security's PasswordEncoder

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       DepartmentRepository departmentRepository, UserMapper userMapper,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserDto> findAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::userToUserDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<UserDto> findUserById(Long id) {
        return userRepository.findById(id).map(userMapper::userToUserDto);
    }

    @Transactional(readOnly = true)
    public Optional<UserDto> findUserByUsername(String username) {
        return userRepository.findByUsername(username).map(userMapper::userToUserDto);
    }

    @Transactional
    public UserDto registerUser(UserCreationRequestDto userDto) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        User user = userMapper.userCreationRequestDtoToUser(userDto);
        user.setPasswordHash(passwordEncoder.encode(userDto.getPassword()));

        Role userRole = roleRepository.findByName(userDto.getRoleName())
            .orElseThrow(() -> new RuntimeException("Error: Role " + userDto.getRoleName() + " not found."));
        user.setRole(userRole);

        if (userDto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(userDto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Error: Department not found with ID: " + userDto.getDepartmentId()));
            user.setDepartment(department);
        }

        user.setActive(true);
        User savedUser = userRepository.save(user);
        return userMapper.userToUserDto(savedUser);
    }

    @Transactional
    public UserDto updateUser(Long id, UserUpdateRequestDto userUpdateDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Update basic fields if provided in DTO
        if (userUpdateDto.getUsername() != null) user.setUsername(userUpdateDto.getUsername());
        if (userUpdateDto.getEmail() != null) user.setEmail(userUpdateDto.getEmail());
        if (userUpdateDto.getFirstName() != null) user.setFirstName(userUpdateDto.getFirstName());
        if (userUpdateDto.getLastName() != null) user.setLastName(userUpdateDto.getLastName());
        if (userUpdateDto.getIsActive() != null) user.setActive(userUpdateDto.getIsActive());

        if (userUpdateDto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(userUpdateDto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Error: Department not found."));
            user.setDepartment(department);
        } else {
             // If departmentId is explicitly passed as null by some convention, or not present, means no change or remove
            // For now, if not present, we don't change. If explicitly null, we could set user.setDepartment(null);
        }


        if (userUpdateDto.getRoleName() != null) {
            Role newRole = roleRepository.findByName(userUpdateDto.getRoleName())
                    .orElseThrow(() -> new RuntimeException("Error: Role not found."));
            user.setRole(newRole);
        }

        // Password update should be handled by a separate, dedicated method for security reasons.
        // e.g., changePassword(Long userId, String oldPassword, String newPassword)

        User updatedUser = userRepository.save(user);
        return userMapper.userToUserDto(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        // Consider business logic: e.g., cannot delete user if they have active processes/goals.
        // Or reassign their tasks. For now, direct deletion.
        userRepository.deleteById(id);
    }

    @Transactional
    public UserDto changeUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Role newRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Error: Role '" + roleName + "' not found."));
        user.setRole(newRole);
        User updatedUser = userRepository.save(user);
        return userMapper.userToUserDto(updatedUser);
    }

    // Authentication logic is typically handled by Spring Security framework itself.
    // This method might be used for custom checks or not needed if using standard Spring Security.
    // public boolean authenticate(String username, String rawPassword) {
    //    Optional<User> userOpt = userRepository.findByUsername(username);
    //    if (userOpt.isPresent()) {
    //        User user = userOpt.get();
    //        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    //    }
    //    return false;
    // }
}
