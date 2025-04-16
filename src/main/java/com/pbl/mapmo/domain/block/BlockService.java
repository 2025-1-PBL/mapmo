package com.pbl.mapmo.domain.block;

import com.pbl.mapmo.domain.blockmember.BlockMember;
import com.pbl.mapmo.domain.user.User;
import com.pbl.mapmo.domain.blockmember.BlockMemberRepository;
import com.pbl.mapmo.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BlockService {

    private final BlockRepository blockRepository;
    private final BlockMemberRepository blockMemberRepository;
    private final UserRepository userRepository;

    @Autowired
    public BlockService(BlockRepository blockRepository,
                        BlockMemberRepository blockMemberRepository,
                        UserRepository userRepository) {
        this.blockRepository = blockRepository;
        this.blockMemberRepository = blockMemberRepository;
        this.userRepository = userRepository;
    }

    /**
     * 사용자가 다른 사용자를 차단합니다.
     *
     * @param userId 차단하는 사용자 ID
     * @param blockedUserId 차단될 사용자 ID
     * @return 생성된 차단 정보
     */
    @Transactional
    public BlockMember blockUser(Integer userId, Integer blockedUserId) {
        // 자기 자신은 차단 불가
        if (userId.equals(blockedUserId)) {
            throw new RuntimeException("자기 자신을 차단할 수 없습니다.");
        }

        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        User blockedUser = userRepository.findById(blockedUserId)
                .orElseThrow(() -> new RuntimeException("차단할 사용자를 찾을 수 없습니다."));

        // 이미 차단되었는지 확인
        if (isUserBlocked(userId, blockedUserId)) {
            throw new RuntimeException("이미 차단한 사용자입니다.");
        }

        // 사용자의 차단 그룹 조회 또는 생성
        Block block = blockRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Block newBlock = new Block();
                    newBlock.setUser(user);
                    newBlock.setBlockMembers(new ArrayList<>());
                    return blockRepository.save(newBlock);
                });

        // 차단 멤버 추가
        BlockMember blockMember = new BlockMember();
        blockMember.setBlock(block);
        blockMember.setUser(blockedUser);

        return blockMemberRepository.save(blockMember);
    }

    /**
     * 사용자가 다른 사용자의 차단을 해제합니다.
     *
     * @param userId 차단 해제하는 사용자 ID
     * @param blockedUserId 차단 해제될 사용자 ID
     */
    @Transactional
    public void unblockUser(Integer userId, Integer blockedUserId) {
        // 차단 그룹 조회
        Block block = blockRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("차단 정보를 찾을 수 없습니다."));

        // 차단 멤버 조회 및 삭제
        BlockMember blockMember = blockMemberRepository.findByBlockIdAndUserId(block.getId(), blockedUserId)
                .orElseThrow(() -> new RuntimeException("차단된 사용자를 찾을 수 없습니다."));

        blockMemberRepository.delete(blockMember);
    }

    /**
     * 사용자가 차단한 모든 사용자 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 차단된 사용자 목록
     */
    public List<User> getBlockedUsers(Integer userId) {
        Optional<Block> blockOpt = blockRepository.findByUserId(userId);

        if (blockOpt.isEmpty()) {
            return new ArrayList<>();
        }

        Block block = blockOpt.get();
        List<BlockMember> blockMembers = blockMemberRepository.findByBlockId(block.getId());

        return blockMembers.stream()
                .map(BlockMember::getUser)
                .collect(Collectors.toList());
    }

    /**
     * 사용자가 차단한 모든 사용자 ID 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 차단된 사용자 ID 목록
     */
    public List<Integer> getBlockedUserIds(Integer userId) {
        Optional<Block> blockOpt = blockRepository.findByUserId(userId);

        if (blockOpt.isEmpty()) {
            return new ArrayList<>();
        }

        Block block = blockOpt.get();
        List<BlockMember> blockMembers = blockMemberRepository.findByBlockId(block.getId());

        return blockMembers.stream()
                .map(blockMember -> blockMember.getUser().getId())
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자가 다른 사용자를 차단했는지 확인합니다.
     *
     * @param userId 확인할 사용자 ID
     * @param blockedUserId 차단 여부를 확인할 사용자 ID
     * @return 차단 여부
     */
    public boolean isUserBlocked(Integer userId, Integer blockedUserId) {
        Optional<Block> blockOpt = blockRepository.findByUserId(userId);

        if (blockOpt.isEmpty()) {
            return false;
        }

        Block block = blockOpt.get();
        return blockMemberRepository.existsByBlockIdAndUserId(block.getId(), blockedUserId);
    }
}