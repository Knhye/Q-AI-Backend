package com.example.qnai.repository;

import com.example.qnai.dto.refreshToken.RefreshDto;
import com.example.qnai.entity.RefreshToken;
import com.example.qnai.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository{
    void save(String token, Users user, long ttl);

    Optional<RefreshDto> findByToken(String token);

    void deleteByToken(String token);

    RefreshToken findByUser(Users user);
}
