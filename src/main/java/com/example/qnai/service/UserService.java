package com.example.qnai.service;

import com.example.qnai.dto.user.request.UserFcmTokenUpdateRequest;
import com.example.qnai.dto.user.request.UserPasswordUpdateRequest;
import com.example.qnai.dto.user.request.UserUpdateRequest;
import com.example.qnai.dto.user.response.UpdateUserPasswordResponse;
import com.example.qnai.dto.user.response.UserDetailResponse;
import com.example.qnai.dto.user.response.UserUpdateResponse;
import com.example.qnai.entity.Users;
import com.example.qnai.global.exception.*;
import com.example.qnai.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserDetailResponse getUserDetail(HttpServletRequest httpServletRequest) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Users user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException("유저가 존재하지 않습니다."));

        return UserDetailResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .mainSubject(user.getMainSubject())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Transactional
    public UserUpdateResponse updateUser(HttpServletRequest httpServletRequest, UserUpdateRequest request) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Users user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException("유저가 존재하지 않습니다."));

        user.updateInfo(
                request.getEmail(),
                request.getNickname(),
                request.getMainSubject()
        );

        return UserUpdateResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .mainSubject(user.getMainSubject())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Transactional
    public UpdateUserPasswordResponse updateUserPassword(HttpServletRequest httpServletRequest, UserPasswordUpdateRequest request) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Users user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException("유저가 존재하지 않습니다."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        String newHashedPassword = passwordEncoder.encode(request.getNewPassword());

        user.updatePassword(newHashedPassword);

        return UpdateUserPasswordResponse.builder()
                .id(user.getId())
                .build();
    }

    @Transactional
    public void updateUserFcmToken(HttpServletRequest httpServletRequest, UserFcmTokenUpdateRequest request) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Users user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저가 존재하지 않습니다."));

        user.updateFcmToken(request.getFcmToken());
    }
}
