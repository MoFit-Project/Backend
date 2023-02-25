package Mofit.com.api.controller;

import Mofit.com.Domain.Member;
import Mofit.com.api.request.MyPageReq;
import Mofit.com.api.request.SignReq;
import Mofit.com.api.response.SignRes;
import Mofit.com.api.service.SignService;
import Mofit.com.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/mofit")
@RequiredArgsConstructor
public class MyPageController {

    private final MemberRepository memberRepository;
    private final SignService memberService;


    @GetMapping("/user/{account}")
    public String myPage(@PathVariable String account) {

        return "ok";
    }
    @PostMapping("/user/{account}")
    public String changeMyPage(@PathVariable String account,@RequestBody MyPageReq sign) throws Exception {    // requestBody 객
        Optional<Member> uId = memberRepository.findByAccount(account);
        if (uId.isPresent()){
            boolean update = memberService.update(account, sign);
            if (update){
                return "성공";
            }
            return "실패";
        }
//        memberRepository.findByAccount(account).isPresent().
        return "계정이 존재 하지 않음";
    }

    @GetMapping("/ranking")
    public String rankPage() {
        return "ok";
    }




}