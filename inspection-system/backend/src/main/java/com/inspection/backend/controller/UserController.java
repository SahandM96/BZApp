package com.inspection.backend.controller;

import com.inspection.backend.dto.UserDto;
import com.inspection.backend.dto.UserCreationRequestDto;
import com.inspection.backend.dto.UserUpdateRequestDto;
import com.inspection.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// import org.springframework.security.access.prepost.PreAuthorize; // For method-level security
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import org.springframework.security.core.userdetails.UserDetails; // Or your custom UserPrincipal for creatorId

import javax.validation.Valid;
import java.util.List;
import java.util.Map; // For simple request like role change

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    // @PreAuthorize("hasRole('ADMIN') or #id == @userSecurity.getUserIdFromPrincipal(authentication)")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return userService.findUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) {
        return userService.findUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // Public endpoint for registration, or admin-only if preferred
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserCreationRequestDto userCreationRequestDto) {
        try {
            UserDto createdUser = userService.registerUser(userCreationRequestDto);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Consider more specific exception handling and error DTOs
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // @PreAuthorize("hasRole('ADMIN') or #id == @userSecurity.getUserIdFromPrincipal(authentication)")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequestDto userUpdateRequestDto) {
         try {
            UserDto updatedUser = userService.updateUser(id, userUpdateRequestDto);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            // Distinguish between Not Found and other Bad Requests
            if (e.getMessage().toLowerCase().contains("not found")) {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            // Log error
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Or internal server error depending on cause
        }
    }

    // @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{userId}/role")
    public ResponseEntity<?> changeUserRole(@PathVariable Long userId, @RequestBody Map<String, String> payload) {
        // Expecting a JSON like {"roleName": "NEW_ROLE_NAME"}
        String roleName = payload.get("roleName");
        if (roleName == null || roleName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error","roleName is required in the request body."));
        }
        try {
            UserDto updatedUser = userService.changeUserRole(userId, roleName);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
             if (e.getMessage().toLowerCase().contains("not found")) {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Login endpoint would typically be handled by Spring Security's filter chain.
    // If you need a custom login controller method (e.g., for token generation), it would go here.
    // For now, assuming standard Spring Security setup handles /login.
}
