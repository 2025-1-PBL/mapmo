package com.pbl.mapmo.domain.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 사용자 이메일로 사용자를 조회합니다.
     *
     * @param email 사용자 이메일
     * @return 조회된 사용자
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * 사용자 ID로 사용자를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 조회된 사용자
     */
    public Optional<User> getUserById(Integer userId) {
        return userRepository.findById(userId);
    }

    /**
     * 새 사용자를 등록합니다. (일반 회원가입)
     *
     * @param user 등록할 사용자 정보
     * @return 등록된 사용자
     */
    @Transactional
    public User registerUser(User user) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        // 비밀번호 암호화
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        user.setIsDeleted(false);
        return userRepository.save(user);
    }

    /**
     * 소셜 로그인 사용자를 등록하거나 조회합니다.
     *
     * @param email 소셜 로그인 이메일
     * @param name 소셜 로그인 이름
     * @param profilePic 프로필 사진 URL
     * @return 등록 또는 조회된 사용자
     */
    @Transactional
    public User registerOrGetSocialUser(String email, String name, String profilePic) {
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            return existingUser.get();
        } else {
            User newUser = User.builder()
                    .email(email)
                    .name(name)
                    .profilePic(profilePic)
                    .password(null)  // 소셜 로그인은 비밀번호 없음
                    .isDeleted(false)
                    .build();

            return userRepository.save(newUser);
        }
    }

    /**
     * 사용자 정보를 수정합니다.
     *
     * @param userId 수정할 사용자 ID
     * @param updatedUser 수정된 사용자 정보
     * @return 수정된 사용자
     */
    @Transactional
    public User updateUser(Integer userId, User updatedUser) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 필드 업데이트
        if (updatedUser.getName() != null) {
            existingUser.setName(updatedUser.getName());
        }

        if (updatedUser.getProfilePic() != null) {
            existingUser.setProfilePic(updatedUser.getProfilePic());
        }

        if (updatedUser.getSex() != null) {
            existingUser.setSex(updatedUser.getSex());
        }

        // 비밀번호 변경 (로컬 회원만 가능)
        if (updatedUser.getPassword() != null && existingUser.getPassword() != null) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    /**
     * 사용자 계정을 삭제합니다. (실제 삭제가 아닌 비활성화)
     *
     * @param userId 삭제할 사용자 ID
     */
    @Transactional
    public void deleteUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 실제 삭제가 아닌 논리적 삭제 처리
        user.setIsDeleted(true);
        userRepository.save(user);
    }

    /**
     * 사용자 로그인을 처리합니다.
     *
     * @param email 사용자 이메일
     * @param password 사용자 비밀번호
     * @return 로그인된 사용자
     */
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 계정 상태 확인
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new RuntimeException("삭제된 계정입니다.");
        }

        // 로컬 회원 로그인인 경우 비밀번호 확인
        if (user.getPassword() != null) {
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new RuntimeException("비밀번호가 일치하지 않습니다.");
            }
        } else {
            // 소셜 로그인인 경우
            throw new RuntimeException("소셜 로그인 계정은 일반 로그인이 불가능합니다.");
        }

        return user;
    }

    /**
     * 사용자 이름으로 사용자를 검색합니다.
     *
     * @param name 검색할 사용자 이름
     * @return 검색된 사용자 목록
     */
    public List<User> searchUsersByName(String name) {
        return userRepository.findByNameContainingAndIsDeletedFalse(name);
    }
}