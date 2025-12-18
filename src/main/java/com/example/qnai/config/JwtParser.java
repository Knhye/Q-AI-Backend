package com.example.qnai.config;

import com.example.qnai.global.exception.InvalidTokenException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;

@Component
public class JwtParser {

    private static final String TOKEN_VALUE_DELIMITER = "\\.";
    private static final int HEADER_INDEX = 0;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public Map<String, String> parseHeaders(String token) {
        try {
            String encodedHeader = token.split(TOKEN_VALUE_DELIMITER)[HEADER_INDEX];
            String decodedHeader = new String(Base64.getUrlDecoder().decode(encodedHeader));
            return OBJECT_MAPPER.readValue(
                    decodedHeader,
                    new TypeReference<Map<String, String>>() {}
            );
        } catch (JsonProcessingException | ArrayIndexOutOfBoundsException e) {
            throw new InvalidTokenException("유효하지 않은 토큰입니다.");
        }
    }

//    public Claims parseClaims(String idToken, PublicKey publicKey) {
//        try {
//            return Jwts.parser()
//                    .verifyWith(publicKey)
//                    .build()
//                    .parseSignedClaims(idToken)
//                    .getPayload();
//        } catch (ExpiredJwtException e) {
//            throw new InvalidTokenException("토큰이 만료되었습니다.");
//        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
//            throw new InvalidTokenException("토큰이 유효하지 않습니다.");
//        }
//    }
}
