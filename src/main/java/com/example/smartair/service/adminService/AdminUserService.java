package com.example.smartair.service.adminService;

import com.example.smartair.dto.userDto.UserDetailResponseDto;
import com.example.smartair.entity.user.User;
import com.example.smartair.repository.userRepository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;
    // RoomParticipantRepository는 User 엔티티에서 직접 참여 방 개수를 가져올 수 있으므로 필수는 아님

    /**
     * 관리자용: 전체 사용자 현황 상세 조회 (페이징)
     * @param pageable 페이징 및 정렬 정보
     * @return Page<UserDetailResponseDto>
     */
    public Page<UserDetailResponseDto> getAllUsersDetailForAdmin(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(UserDetailResponseDto::from);
    }
} 