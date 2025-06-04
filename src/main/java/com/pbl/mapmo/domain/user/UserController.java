package com.pbl.mapmo.domain.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 이메일로 사용자 조회
     */
    @Operation(summary = "이메일로 사용자 조회")
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userService.getUserByEmail(email);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * ID로 사용자 조회
     */
    @Operation(summary = "ID로 사용자 조회")
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Integer userId) {
        Optional<User> user = userService.getUserById(userId);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 새 사용자 등록 (회원가입)
     */
    @Operation(summary = "새 사용자 등록 (회원가입)")
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 소셜 로그인 처리
     */
    @Operation(summary = "소셜 로그인 처리")
    @PostMapping("/social-login")
    public ResponseEntity<User> socialLogin(@RequestBody Map<String, String> socialUserData) {
        String email = socialUserData.get("email");
        String name = socialUserData.get("name");
        String profilePic = socialUserData.get("profilePic");

        if (email == null || name == null) {
            return ResponseEntity.badRequest().build();
        }

        User user = userService.registerOrGetSocialUser(email, name, profilePic);
        return ResponseEntity.ok(user);
    }

    /**
     * 사용자 로그인
     */
    @Operation(summary = "사용자 로그인")
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody Map<String, String> loginData) {
        try {
            String email = loginData.get("email");
            String password = loginData.get("password");

            if (email == null || password == null) {
                return ResponseEntity.badRequest().build();
            }

            User user = userService.login(email, password);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * 사용자 정보 수정
     */
    @Operation(summary = "사용자 정보 수정")
    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable Integer userId, @RequestBody User updatedUser) {
        try {
            User user = userService.updateUser(userId, updatedUser);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 사용자 계정 삭제 (비활성화)
     */
    @Operation(summary = "사용자 계정 삭제 (비활성화)")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 사용자 이름으로 검색
     */
    @Operation(summary = "사용자 이름으로 검색")
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsersByName(@RequestParam String name) {
        List<User> users = userService.searchUsersByName(name);
        return ResponseEntity.ok(users);
    }

    // UserController.java에 추가
    @PostMapping("/{userId}/make-admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 기존 관리자만 접근 가능
    public ResponseEntity<User> makeAdmin(@PathVariable Integer userId) {
        try {
            User user = userService.addRoleToUser(userId, "ROLE_ADMIN");
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // UserController.java에 추가
    @GetMapping("/admins")
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자만 접근 가능
    public ResponseEntity<List<UserDto.Response>> getAdminUsers() {
        List<User> adminUsers = userService.getUsersByAuthority("ROLE_ADMIN");
        return ResponseEntity.ok(UserDto.Response.of(adminUsers));
    }
}