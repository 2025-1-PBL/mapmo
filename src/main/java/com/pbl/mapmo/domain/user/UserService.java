package com.pbl.mapmo.domain.user;

import com.pbl.mapmo.domain.authority.Authority;
import com.pbl.mapmo.domain.authority.AuthorityRepository;
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
    private final AuthorityRepository authorityRepository;  // 새로 추가

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthorityRepository authorityRepository) {  // 생성자 매개변수 추가
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authorityRepository = authorityRepository;
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
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        // 비밀번호 암호화
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 사용자 저장
        User savedUser = userRepository.save(user);

        // 기본 권한 추가 (ROLE_USER)
        Authority authority = new Authority();
        authority.setUser(savedUser);
        authority.setAuthorityName("ROLE_USER");
        authorityRepository.save(authority);

        return savedUser;
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
                    .isDeleted(false)
                    .build();

            User savedUser = userRepository.save(newUser);

            // 소셜 로그인 사용자에게도 기본 권한 추가
            Authority authority = new Authority();
            authority.setUser(savedUser);
            authority.setAuthorityName("ROLE_USER");
            authorityRepository.save(authority);

            return savedUser;
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

        // 수정할 필드들 업데이트
        if (updatedUser.getName() != null) {
            existingUser.setName(updatedUser.getName());
        }
        if (updatedUser.getProfilePic() != null) {
            existingUser.setProfilePic(updatedUser.getProfilePic());
        }
        if (updatedUser.getSex() != null) {
            existingUser.setSex(updatedUser.getSex());
        }
        if (updatedUser.getPassword() != null) {
            // 비밀번호 변경 시 암호화
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

        if (user.getPassword() == null) {
            throw new RuntimeException("소셜 로그인 사용자입니다.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new RuntimeException("삭제된 사용자입니다.");
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

    /**
     * 사용자에게 역할을 추가합니다.
     *
     * @param userId 사용자 ID
     * @param roleName 추가할 역할 이름 (예: "ROLE_ADMIN")
     * @return 역할이 추가된 사용자
     */
    @Transactional
    public User addRoleToUser(Integer userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 이미 같은 역할이 있는지 확인
        boolean hasRole = user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthorityName().equals(roleName));

        if (!hasRole) {
            Authority authority = new Authority();
            authority.setUser(user);
            authority.setAuthorityName(roleName);
            authorityRepository.save(authority);
        }

        return user;
    }

    /**
     * 사용자의 역할을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자의 역할 목록
     */
    public List<Authority> getUserAuthorities(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return authorityRepository.findByUser(user);
    }

    // UserService.java에 추가
    public List<User> getUsersByAuthority(String authority) {
        return userRepository.findByAuthority(authority);
    }
}