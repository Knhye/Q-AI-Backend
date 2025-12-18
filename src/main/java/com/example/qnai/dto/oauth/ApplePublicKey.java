package com.example.qnai.dto.oauth;

import com.example.qnai.global.exception.InvalidTokenException;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ApplePublicKey {
    private String kty;
    private String kid;
    private String use;
    private String alg;
    private String n;
    private String e;

//    public ApplePublicKey getMatchedKey(
//            ApplePublicKeyResponse response,
//            String kid,
//            String alg
//    ) {
//        return response.getKeys().stream()
//                .filter(key ->
//                        key.getKid().equals(kid)
//                                && key.getAlg().equals(alg))
//                .findFirst()
//                .orElseThrow(() -> new InvalidTokenException("토큰이 유효하지 않습니다."));
//    }
}
