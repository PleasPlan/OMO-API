package com.OmObe.OmO.auth.jwt;

import com.OmObe.OmO.exception.BusinessLogicException;
import com.OmObe.OmO.exception.ExceptionCode;
import com.OmObe.OmO.member.entity.Member;
import com.OmObe.OmO.member.repository.MemberRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Component
public class TokenDecryption {
    private final MemberRepository memberRepository;

    public TokenDecryption(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // JWT 토큰을 해석하여 토큰 사용자를 알아내는 함수
    public Member getWriterInJWTToken(String token) throws JsonProcessingException {
        if(token != null) {
            String[] chunks = token.split("\\.");

            Base64.Decoder decoder = Base64.getUrlDecoder();

            String payload = new String(decoder.decode(chunks[1]));

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> returnMap = objectMapper.readValue(payload, Map.class);

            Object objectWriter = returnMap.get("sub");
            String email = objectWriter.toString();

            Optional<Member> member = memberRepository.findByEmail(email);
            Member writer = member.orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
            return writer;
        }
        else {
            return null;
        }
    }
}
