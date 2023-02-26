package Mofit.com.api.service;

import Mofit.com.Domain.Authority;
import Mofit.com.Domain.Member;
import Mofit.com.api.request.MyPageReq;
import Mofit.com.api.request.SignReq;
import Mofit.com.api.response.SignRes;
import Mofit.com.repository.MemberRepository;
import Mofit.com.security.JwtProvider;
import Mofit.com.security.Token;
import Mofit.com.security.TokenDto;
import Mofit.com.security.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SignService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final JwtProvider jwtProvider;

    public SignRes login(SignReq request) throws Exception {
        Member member = memberRepository.findByAccount(request.getAccount()).orElseThrow(() ->
                new BadCredentialsException("잘못된 계정정보입니다."));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new BadCredentialsException("잘못된 계정정보입니다.");
        }

        member.setRefreshToken(createRefreshToken(member));

        return SignRes.builder()
                .id(member.getId())
                .account(member.getAccount())
                .nickname(member.getNickname())
                .roles(member.getRoles())
                .token(TokenDto.builder()
                        .access_token(jwtProvider.createToken(member.getAccount(), member.getRoles()))
                        .refresh_token(member.getRefreshToken())
                        .build())
                .build();
    }

    public boolean update(String account, MyPageReq request) throws Exception {
        try {
            Optional<Member> check = memberRepository.findByAccount(account);
            if (check.isPresent()){
//                Member member = Member.builder()
//                        .account(check.get().getAccount())
//                        .password(passwordEncoder.encode(request.getPassword()))
//                        .nickname(request.getNickname())
//                        .build();
//                member.setRoles(Collections.singletonList(Authority.builder().name("ROLE_USER").build()));c
                check.get().setPassword(passwordEncoder.encode(request.getPassword()));
                check.get().setNickname(request.getNickname());


                memberRepository.save(check.get());

                return true;
            }
        }catch (Exception e) {
            log.error("erroer = {}",e.getMessage());
            throw new Exception("오류 발생.");
        }

        return false;
    }

    public boolean register(SignReq request) throws Exception {
        try {
            Optional<Member> check = memberRepository.findByAccount(request.getAccount());

            if (check.isPresent()){
                return  false;
            }

            Member member = Member.builder()
                    .account(request.getAccount())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .nickname(request.getNickname())
                    .build();

            member.setRoles(Collections.singletonList(Authority.builder().name("ROLE_USER").build()));

            memberRepository.save(member);
        } catch (Exception e) {
            log.error("erroer = {}",e.getMessage());
            throw new Exception("잘못된 요청입니다.");
        }
        return true;
    }

    public SignRes getMember(String account) throws Exception {
        Member member = memberRepository.findByAccount(account)
                .orElseThrow(() -> new Exception("계정을 찾을 수 없습니다."));
        return new SignRes(member);
    }
    // Refresh Token ================

    /**
     * Refresh 토큰을 생성한다.
     * Redis 내부에는
     * refreshToken:memberId : tokenValue
     * 형태로 저장한다.
     */
    public String createRefreshToken(Member member) {
        Token token = tokenRepository.save(
                Token.builder()
                        .id(member.getId())
                        .refresh_token(UUID.randomUUID().toString())
                        .expiration(120)
                        .build()
        );
        return token.getRefresh_token();
    }

    public Token validRefreshToken(Member member, String refreshToken) throws Exception {
        Token token = tokenRepository.findById(member.getId()).orElseThrow(() -> new Exception("만료된 계정입니다. 로그인을 다시 시도하세요"));
        // 해당유저의 Refresh 토큰 만료 : Redis에 해당 유저의 토큰이 존재하지 않음
        if (token.getRefresh_token() == null) {
            return null;
        } else {
            // 리프레시 토큰 만료일자가 얼마 남지 않았을 때 만료시간 연장..?
            if(token.getExpiration() < 10) {
                token.setExpiration(1000);
                tokenRepository.save(token);
            }

            // 토큰이 같은지 비교
            if(!token.getRefresh_token().equals(refreshToken)) {
                return null;
            } else {
                return token;
            }
        }
    }

    public TokenDto refreshAccessToken(TokenDto token) throws Exception {
        String account = jwtProvider.getAccount(token.getAccess_token());
        Member member = memberRepository.findByAccount(account).orElseThrow(() ->
                new BadCredentialsException("잘못된 계정정보입니다."));
        Token refreshToken = validRefreshToken(member, token.getRefresh_token());

        if (refreshToken != null) {
            return TokenDto.builder()
                    .access_token(jwtProvider.createToken(account, member.getRoles()))
                    .refresh_token(refreshToken.getRefresh_token())
                    .build();
        } else {
            throw new Exception("로그인을 해주세요");
        }
    }
}