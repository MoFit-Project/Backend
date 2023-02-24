package Mofit.com.api.controller;

import Mofit.com.api.request.SignReq;
import Mofit.com.api.response.SignRes;
import Mofit.com.api.service.SignService;
import Mofit.com.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
    public String changeMyPage(@PathVariable String account) {

        return "ok";
    }




}
