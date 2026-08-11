package com.lounge.domain.user.service;

import com.lounge.domain.user.dto.response.UserMeResponse;
import com.lounge.global.api.code.GeneralErrorCode;
import com.lounge.global.exception.GeneralException;
import com.lounge.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserMeResponse getMe(Long userId) {
        return userRepository.findById(userId)
                .map(user -> UserMeResponse.of(user.getUsername()))
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));
    }
}
