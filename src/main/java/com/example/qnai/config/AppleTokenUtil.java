package com.example.qnai.config;

import com.example.qnai.dto.oauth.ApplePublicKey;
import com.example.qnai.global.exception.InvalidTokenException;
import com.example.qnai.service.AppleOAuthService;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.InternalException;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AppleTokenUtil {

    private final AppleOAuthService appleOAuthService;
    private final JwtParser jwtParser;

    public Map<String, Object> validateAndParse(String idToken) {
        try {
            Map<String, String> headers = jwtParser.parseHeaders(idToken);
            String kid = headers.get("kid");
            String alg = headers.get("alg");

            if (!"RS256".equals(alg)) {
                throw new InvalidTokenException("토큰이 형식이 잘못되었습니다.");
            }

            ApplePublicKey matchedKey = appleOAuthService.getAppleKeys().getKeys().stream()
                    .filter(k -> k.getKid().equals(kid))
                    .findFirst()
                    .orElseThrow(() -> new InvalidTokenException("토큰 형식이 잘못되었습니다."));

            byte[] nBytes = Base64.getUrlDecoder().decode(matchedKey.getN());
            byte[] eBytes = Base64.getUrlDecoder().decode(matchedKey.getE());

            RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
                    .generatePublic(new RSAPublicKeySpec(
                            new BigInteger(1, nBytes),
                            new BigInteger(1, eBytes)
                    ));

            SignedJWT signedJWT = SignedJWT.parse(idToken);
            JWSVerifier verifier = new RSASSAVerifier(publicKey);

            if (!signedJWT.verify(verifier)) {
                throw new InvalidTokenException("유효하지 않은 토큰입니다.");
            }

            return signedJWT.getJWTClaimsSet().getClaims();

        } catch (Exception e) {
            throw new InternalException("서버에서 로그인 중 오류가 발생하였습니다.", e);
        }
    }

}
