package com.example.qnai.dto.oauth;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ApplePublicKeyResponse {
    private List<ApplePublicKey> keys;
}
