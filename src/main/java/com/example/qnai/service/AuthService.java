package com.example.qnai.service;

import com.example.qnai.config.AppleTokenUtil;
import com.example.qnai.config.TokenProvider;
import com.example.qnai.dto.oauth.AppleSignInRequest;
import com.example.qnai.dto.refreshToken.RefreshDto;
import com.example.qnai.dto.refreshToken.request.RefreshRequest;
import com.example.qnai.dto.refreshToken.response.RefreshResponse;
import com.example.qnai.dto.user.request.DeleteUserRequest;
import com.example.qnai.dto.user.request.LoginRequest;
import com.example.qnai.dto.user.request.LogoutRequest;
import com.example.qnai.dto.user.request.SignupRequest;
import com.example.qnai.dto.user.response.LoginResponse;
import com.example.qnai.dto.user.response.SignupResponse;
import com.example.qnai.entity.RefreshToken;
import com.example.qnai.entity.UserNotificationSetting;
import com.example.qnai.entity.Users;
import com.example.qnai.global.exception.*;
import com.example.qnai.repository.BlacklistRepository;
import com.example.qnai.repository.RefreshTokenRepository;
import com.example.qnai.repository.UserNotificationSettingRepository;
import com.example.qnai.repository.UserRepository;
import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserNotificationSettingRepository userNotificationSettingRepository;
    private final BlacklistRepository blacklistRepository;
    private final AppleTokenUtil appleTokenUtil;


    public SignupResponse signup(SignupRequest request){
        if(userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())){
            throw new UserAlreadyExistException("이미 가입된 이메일입니다.");
        }

        Users user = Users.builder()
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .email(request.getEmail())
                .build();

        UserNotificationSetting notificationSetting = UserNotificationSetting.builder()
                .user(user)
                .build();

        user.setNotification(notificationSetting);

        userRepository.save(user);

        userNotificationSettingRepository.save(notificationSetting);

        return SignupResponse.builder()
                .userId(user.getId())
                .build();
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        // ID, PW 검증
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );


        Users user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User doesn't exist"));

        String accessToken = tokenProvider.createAccessToken(request.getEmail());

        String refreshToken = tokenProvider.createRefreshToken(request.getEmail());

        long refreshTokenTtl = 7 * 24 * 60 * 60; // 7일 (초 단위)
        refreshTokenRepository.save(refreshToken, user, refreshTokenTtl);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public LoginResponse appleLogin(AppleSignInRequest request){
        Map<String, Object> claims = appleTokenUtil.validateAndParse(request.getIdentityToken());

        String appleSub = (String) claims.get("sub");
        String email = (String) claims.get("email");

        Users user = findOrCreate(appleSub, email);

        String accessToken = tokenProvider.createAccessToken(user.getEmail());
        String refreshToken = tokenProvider.createRefreshToken(user.getEmail());

        saveRefreshToken(user, refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public RefreshResponse refresh(RefreshRequest requestDto){

        RefreshDto existingRefreshToken = refreshTokenRepository.findByToken(requestDto.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException("토큰을 찾을 수 없습니다."));

        if(existingRefreshToken.getExpiryDatetime().isBefore(LocalDateTime.now())){
            refreshTokenRepository.deleteByToken(requestDto.getRefreshToken());
            throw new InvalidTokenException("토큰이 만료되었습니다.");
        }

        String username = tokenProvider.extractUsername(existingRefreshToken.getToken());
        String newAccessToken = tokenProvider.createAccessToken(username);

        return RefreshResponse.builder()
                .accessToken(newAccessToken)
                .build();
    }

    @Transactional
    public void logout(HttpServletRequest httpServletRequest, LogoutRequest request) {
        String accessToken = extractAccessToken(httpServletRequest);
        if (accessToken == null) {
            throw new NotLoggedInException("로그인이 필요한 요청입니다.");
        }

        if (!tokenProvider.validateToken(accessToken)) {
            throw new InvalidTokenException("유효하지 않은 Access Token입니다.");
        }

        Long expiration = tokenProvider.getExpiration(accessToken);
        if (expiration > 0) {
            blacklistRepository.addToBlacklist(accessToken, expiration);
        }

        String refreshToken = request.getRefreshToken();
        if (refreshToken != null && !refreshToken.isEmpty()) {
            refreshTokenRepository.deleteByToken(refreshToken);
        }

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        UserNotificationSetting userNotificationSetting =
                userNotificationSettingRepository.findByUserEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("푸시 알림을 설정할 수 없습니다."));

        userNotificationSetting.unsubscribe();
    }

    @Transactional
    public void deleteUser(HttpServletRequest httpServletRequest, DeleteUserRequest request) {
        String bearerToken = httpServletRequest.getHeader("Authorization");
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new InvalidTokenException("토큰 형식이 잘못되었거나 유효하지 않습니다.");
        }
        String accessToken = bearerToken.substring(7);

        if (!tokenProvider.validateToken(accessToken)) {
            throw new InvalidTokenException("유효하지 않거나 만료된 Access Token입니다.");
        }

        String email = tokenProvider.extractUsername(accessToken);

        Users user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException("유저가 존재하지 않습니다."));

        RefreshDto refreshDto = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException("토큰이 존재하지 않습니다."));

        if (!refreshDto.getUserId().equals(user.getId())) {
            throw new NotAcceptableUserException("요청에 포함된 Refresh Token이 현재 유저의 토큰이 아닙니다.");
        }

        refreshTokenRepository.deleteByToken(request.getRefreshToken());


        long remainingTtlSeconds = tokenProvider.getExpiration(accessToken);
        blacklistRepository.addToBlacklist(accessToken, remainingTtlSeconds);

        user.delete();
        userRepository.save(user);
    }

    private String extractAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 제거
        }

        return null;
    }

    private Users findOrCreate(String appleSub, String email) {
        return userRepository.findByAppleSub(appleSub)
                .orElseGet(() -> userRepository.save(
                        Users.builder()
                                .appleSub(appleSub)
                                .email(email)
                                .build()
                ));
    }

    private void saveRefreshToken(Users user, String refreshToken) {
        RefreshToken existingRefreshToken = refreshTokenRepository.findByUser(user);
        existingRefreshToken.updateToken(refreshToken);
        userRepository.save(user);
    }
}
